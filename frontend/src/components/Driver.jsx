import { useEffect, useRef, useState } from 'react';
import { api } from '../api.js';
import { subscribe, publish } from '../ws.js';
import { STATUS, inr } from '../places.js';

export default function Driver() {
  const [online, setOnline] = useState(false), [reqs, setReqs] = useState([]), [active, setActive] = useState(null);
  const [stats, setStats] = useState(null), [rank, setRank] = useState(null), [otp, setOtp] = useState(''), [err, setErr] = useState('');

  useEffect(() => { api('/api/driver/history').then((l) => { const a = l.find((r) => ['ACCEPTED', 'ARRIVED', 'IN_TRIP'].includes(r.status)); if (a) { setActive(a); setOnline(true); } }).catch(() => {}); }, []);
  const load = () => { api('/api/driver/requests').then(setReqs).catch(() => {}); api('/api/driver/earnings').then(setStats).catch(() => {}); api('/api/drivers/ranking').then((l) => setRank(l.find((e) => e.you)?.rank ?? null)).catch(() => {}); };
  useEffect(() => { load(); return subscribe('/user/queue/requests', (r) => setReqs((x) => [r, ...x.filter((y) => y.id !== r.id)])); }, []);

  // While online, keep the server's idea of our position fresh (used to match nearby requests), and stream it to the rider during a trip.
  const live = active && ['ACCEPTED', 'ARRIVED', 'IN_TRIP'].includes(active.status);
  const lastPost = useRef(0);
  useEffect(() => {
    if (!online || !navigator.geolocation) return;
    const w = navigator.geolocation.watchPosition((p) => {
      const { latitude: lat, longitude: lng } = p.coords;
      if (live) publish(`/app/ride/${active.id}/location`, { lat, lng });
      if (Date.now() - lastPost.current > 10000) { lastPost.current = Date.now(); api('/api/driver/location', { method: 'POST', body: { lat, lng } }).catch(() => {}); }
    }, () => {}, { enableHighAccuracy: true });
    return () => navigator.geolocation.clearWatch(w);
  }, [online, live, active?.id]);

  const run = (p, opts) => api(p, { method: 'POST', ...opts }).then((r) => { setErr(''); return r; }).catch((e) => { setErr(e.message); load(); });
  const toggle = () => run(`/api/driver/online?value=${!online}`).then((r) => { if (r) { setOnline(r.online); load(); } });
  const accept = (id) => run(`/api/driver/rides/${id}/accept`).then((r) => { if (r) { setActive(r); setReqs((x) => x.filter((y) => y.id !== id)); } });
  const step = (action, q = '') => run(`/api/driver/rides/${active.id}/${action}${q}`).then((r) => { if (r) { setActive(r); setOtp(''); if (r.status === 'COMPLETED') load(); } });

  return (
    <div className="mx-auto max-w-2xl">
      <div className="card">
        <div className="flex items-center justify-between"><h2 className="text-2xl font-extrabold">Driver dashboard</h2>
          <button onClick={toggle} className={`rounded-full border px-4 py-1.5 font-semibold ${online ? 'border-ok text-ok' : 'border-line text-mut'}`}>{online ? '● Online' : '○ Offline'}</button></div>
        <div className="mt-4 grid grid-cols-2 gap-3 text-center sm:grid-cols-4">
          {[['Earnings', stats ? inr(stats.earnings) : '–'], ['Trips', stats?.trips ?? '–'], ['Rating', stats ? `★ ${stats.rating}` : '–'], ['Rank', rank ? `#${rank}` : '–']].map(([k, v]) =>
            <div key={k} className="rounded-xl bg-bg p-3"><div className="text-sm text-mut">{k}</div><div className="font-display text-xl font-extrabold">{v}</div></div>)}
        </div>
        {err && <p className="mt-3 text-sm font-semibold text-bad" role="alert">{err}</p>}
      </div>

      {active && active.status !== 'COMPLETED' && <div className="card mt-4 border-pop">
        <p className="text-sm font-semibold text-mut">Ride #{active.id} · {active.pickupName} to {active.dropName}</p>
        <h3 className="text-xl font-extrabold">{STATUS[active.status]}</h3>
        {active.status === 'ACCEPTED' && <button className="btn mt-3" onClick={() => step('arrive')}>I have arrived</button>}
        {active.status === 'ARRIVED' && <div className="mt-3"><label className="lbl" htmlFor="otp">Rider's 4-digit code</label>
          <input id="otp" inputMode="numeric" maxLength={4} className="input" value={otp} onChange={(e) => setOtp(e.target.value)} />
          <button className="btn mt-3" disabled={otp.length !== 4} onClick={() => step('start', `?otp=${otp}`)}>Start trip</button></div>}
        {active.status === 'IN_TRIP' && <button className="btn mt-3" onClick={() => step('complete')}>Complete trip · {inr(active.fare)}</button>}
      </div>}
      {active?.status === 'COMPLETED' && <div className="card mt-4"><b>Trip completed.</b> Waiting for the rider's payment.<button className="btn-ghost mt-3" onClick={() => setActive(null)}>Back to requests</button></div>}

      {!active && <div className="mt-4 space-y-3">
        <h3 className="text-lg font-bold">Ride requests</h3>
        {!online && <p className="text-mut">Go online to accept rides.</p>}
        {online && reqs.length === 0 && <p className="text-mut">No open requests right now. New ones appear here instantly.</p>}
        {online && reqs.map((r) => <div key={r.id} className="card">
          <div className="flex justify-between"><b>{r.pickupName} to {r.dropName}</b><b>{inr(r.fare)}</b></div>
          <p className="text-sm text-mut">{r.distanceKm} km trip{r.pickupKm != null && ` · pickup ${r.pickupKm} km away`}</p>
          <button className="btn mt-3" onClick={() => accept(r.id)}>Accept ride</button></div>)}
      </div>}
    </div>
  );
}
