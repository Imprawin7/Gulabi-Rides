import { useEffect, useState } from 'react';
import { api, downloadReceipt } from '../api.js';
import { subscribe, publish } from '../ws.js';
import { PLACES, TYPES, STATUS, ORDER, inr } from '../places.js';
import MapView from './MapView.jsx';
import Pay from './Pay.jsx';

const DONE = ['PAID', 'CANCELLED'];

export default function Rider() {
  const [pk, setPk] = useState('Jaipur Junction'), [dp, setDp] = useState('Hawa Mahal');
  const [type, setType] = useState('MINI'), [promo, setPromo] = useState('');
  const [quote, setQuote] = useState(null), [ride, setRide] = useState(null), [car, setCar] = useState(null);
  const [err, setErr] = useState(''), [stars, setStars] = useState(0), [comment, setComment] = useState(''), [rated, setRated] = useState(false);

  useEffect(() => { api('/api/rides/mine').then((l) => { const a = l.find((r) => !DONE.includes(r.status)); if (a) setRide(a); }).catch(() => {}); }, []);

  useEffect(() => {
    if (pk === dp) { setQuote(null); return; }
    const [a, b] = [PLACES[pk], PLACES[dp]];
    const t = setTimeout(() => api(`/api/rides/quote?pLat=${a[0]}&pLng=${a[1]}&dLat=${b[0]}&dLng=${b[1]}&type=${type}&promo=${encodeURIComponent(promo)}`).then(setQuote).catch(() => setQuote(null)), 250);
    return () => clearTimeout(t);
  }, [pk, dp, type, promo]);

  const id = ride?.id;
  useEffect(() => {
    if (!id) return;
    const refresh = () => api('/api/rides/mine').then((l) => { const r = l.find((x) => x.id === id); if (r) setRide(r); });
    const u1 = subscribe(`/topic/ride/${id}`, refresh);
    const u2 = subscribe(`/topic/ride/${id}/location`, (l) => setCar([l.lat, l.lng]));
    return () => { u1(); u2(); };
  }, [id]);

  async function book() {
    setErr('');
    const [a, b] = [PLACES[pk], PLACES[dp]];
    try {
      setCar(null); setStars(0); setComment(''); setRated(false);
      setRide(await api('/api/rides', { method: 'POST', body: { pickupName: pk, pickupLat: a[0], pickupLng: a[1], dropName: dp, dropLat: b[0], dropLng: b[1], vehicleType: type, promoCode: promo } }));
    } catch (e) { setErr(e.message); }
  }
  const cancel = () => api(`/api/rides/${id}/cancel`, { method: 'POST' }).then(setRide).catch((e) => setErr(e.message));
  const submitReview = () => api(`/api/rides/${id}/rating`, { method: 'POST', body: { stars, comment: comment.trim() || null } }).then(() => { setRated(true); setErr(''); }).catch((e) => setErr(e.message));
  const receipt = () => downloadReceipt(id).catch((e) => setErr(e.message));
  const sos = () => publish('/app/sos', { rideId: id, message: 'Rider needs help', lat: car?.[0] ?? PLACES[ride.pickupName][0], lng: car?.[1] ?? PLACES[ride.pickupName][1] });

  const live = ride && ride.status !== 'CANCELLED';
  const options = Object.keys(PLACES).map((k) => <option key={k}>{k}</option>);

  return (
    <div className="grid gap-5 lg:grid-cols-[1.2fr_.8fr]">
      <MapView pickup={PLACES[ride?.pickupName ?? pk]} drop={PLACES[ride?.dropName ?? dp]} car={car} />
      <section className="card self-start">
        {!live ? <>
          <h2 className="text-2xl font-extrabold">{ride?.status === 'CANCELLED' ? 'Ride cancelled' : 'Where to?'}</h2>
          <label className="lbl" htmlFor="pk">Pickup</label>
          <select id="pk" className="input" value={pk} onChange={(e) => setPk(e.target.value)}>{options}</select>
          <label className="lbl" htmlFor="dp">Drop-off</label>
          <select id="dp" className="input" value={dp} onChange={(e) => setDp(e.target.value)}>{options}</select>
          <div className="mt-4 grid grid-cols-3 gap-2">
            {Object.entries(TYPES).map(([k, v]) => (
              <button key={k} onClick={() => setType(k)} className={`rounded-xl border-2 bg-bg p-2.5 text-left ${type === k ? 'border-pop' : 'border-line'}`}>
                <b className="block">{v}</b><span className="text-sm text-mut">{k === type && quote ? inr(quote.fare) : '·'}</span>
              </button>))}
          </div>
          <label className="lbl" htmlFor="promo">Promo code</label>
          <input id="promo" className="input" placeholder="Try WELCOME20" value={promo} onChange={(e) => setPromo(e.target.value)} />
          {quote && <p className="mt-3 text-sm text-mut">{quote.km} km · about {quote.etaMinutes} min{Number(quote.discount) > 0 && ` · you save ${inr(quote.discount)}`}</p>}
          {err && <p className="mt-2 text-sm font-semibold text-bad" role="alert">{err}</p>}
          <button className="btn mt-4" disabled={!quote || pk === dp} onClick={book}>{quote ? `Book ${TYPES[type]} · ${inr(quote.fare)}` : 'Choose different places'}</button>
        </> : <>
          <p className="text-sm font-semibold text-mut">Ride #{ride.id} · {ride.pickupName} to {ride.dropName}</p>
          <h2 className="mt-1 text-2xl font-extrabold">{STATUS[ride.status]}</h2>
          <div className="my-3 flex gap-1">{ORDER.map((s, i) => <span key={s} className={`h-1.5 flex-1 rounded ${i <= ORDER.indexOf(ride.status) ? 'bg-pop' : 'bg-line'}`} />)}</div>
          {ride.driverName && <p><b>{ride.driverName}</b><br /><span className="text-mut">{ride.vehicle}</span></p>}
          {ride.status === 'REQUESTED' && <button className="btn-ghost mt-4" onClick={cancel}>Cancel request</button>}
          {ride.status === 'ACCEPTED' && <><button className="btn-ghost mt-4" onClick={cancel}>Cancel ride</button></>}
          {['ACCEPTED', 'ARRIVED'].includes(ride.status) && ride.otp && <div className="my-4 text-center"><div className="text-sm text-mut">Share this code with your driver to start</div><div className="font-display text-4xl font-extrabold tracking-[.2em]">{ride.otp}</div></div>}
          {['ACCEPTED', 'ARRIVED', 'IN_TRIP'].includes(ride.status) && <button className="mt-3 w-full rounded-xl bg-bad px-4 py-3 font-semibold text-white" onClick={sos}>Send SOS alert</button>}
          {ride.status === 'COMPLETED' && <><p className="mt-3 text-3xl font-extrabold font-display">{inr(ride.fare)}</p><Pay ride={ride} /></>}
          {ride.status === 'PAID' && <>
            <p className="mt-3 text-mut">Receipt: {inr(ride.fare)} · {ride.distanceKm} km</p>
            {rated ? <p className="mt-3 text-center font-semibold" role="status">Thanks for your feedback.</p> : <div className="mt-3 text-center">
              <div className="text-sm text-mut">Rate {ride.driverName?.split(' ')[0]}</div>
              <div>{[1, 2, 3, 4, 5].map((n) => <button key={n} aria-label={`${n} stars`} onClick={() => setStars(n)} className={`p-1 text-3xl ${n <= stars ? 'text-pop' : 'text-line'}`}>★</button>)}</div>
              {stars > 0 && <>
                <label className="sr-only" htmlFor="review">Comment (optional)</label>
                <textarea id="review" className="input mt-2 min-h-[80px]" maxLength={500} placeholder="Add a comment (optional)" value={comment} onChange={(e) => setComment(e.target.value)} />
                <button className="btn mt-2" onClick={submitReview}>Submit review</button></>}
            </div>}
            <button className="btn-ghost mt-3" onClick={receipt}>Download PDF receipt</button>
            <button className="btn mt-2" onClick={() => setRide(null)}>Book another ride</button>
          </>}
          {err && <p className="mt-2 text-sm font-semibold text-bad" role="alert">{err}</p>}
        </>}
      </section>
    </div>
  );
}
