import { useEffect, useState } from 'react';
import { api } from '../api.js';

export default function Support({ user }) {
  const [tickets, setTickets] = useState([]), [rides, setRides] = useState([]);
  const [f, setF] = useState({ subject: '', body: '', rideId: '' });
  const [msg, setMsg] = useState(''), [busy, setBusy] = useState(false);
  const isRider = user.role === 'RIDER';
  const load = () => api('/api/support/mine').then(setTickets).catch(() => {});
  useEffect(() => { load(); if (isRider) api('/api/rides/mine').then(setRides).catch(() => {}); }, [isRider]);

  async function submit(e) {
    e.preventDefault(); setBusy(true); setMsg('');
    try {
      await api('/api/support', { method: 'POST', body: { subject: f.subject, body: f.body, rideId: f.rideId ? Number(f.rideId) : null } });
      setF({ subject: '', body: '', rideId: '' }); setMsg('Thanks, we have your message and will get back to you.'); load();
    } catch (x) { setMsg(x.message); } finally { setBusy(false); }
  }

  return (
    <div className="mx-auto max-w-2xl">
      <form onSubmit={submit} className="card">
        <h2 className="text-2xl font-extrabold">How can we help?</h2>
        <label className="lbl" htmlFor="subject">Subject</label>
        <input id="subject" className="input" required maxLength={120} value={f.subject} onChange={(e) => setF({ ...f, subject: e.target.value })} />
        {isRider && rides.length > 0 && <>
          <label className="lbl" htmlFor="ride">Related trip (optional)</label>
          <select id="ride" className="input" value={f.rideId} onChange={(e) => setF({ ...f, rideId: e.target.value })}>
            <option value="">None</option>
            {rides.map((r) => <option key={r.id} value={r.id}>#{r.id} · {r.pickupName} to {r.dropName}</option>)}
          </select>
        </>}
        <label className="lbl" htmlFor="body">Message</label>
        <textarea id="body" className="input min-h-[120px]" required maxLength={2000} value={f.body} onChange={(e) => setF({ ...f, body: e.target.value })} />
        {msg && <p className="mt-3 text-sm font-semibold" role="status">{msg}</p>}
        <button className="btn mt-4" disabled={busy}>{busy ? 'Sending…' : 'Send message'}</button>
      </form>
      {tickets.length > 0 && <div className="mt-5">
        <h3 className="text-lg font-bold">Your tickets</h3>
        <div className="mt-2 space-y-2">{tickets.map((t) => <div key={t.id} className="card flex justify-between !py-3"><span>#{t.id} · {t.subject}</span><span className="text-sm font-semibold text-mut">{t.status}</span></div>)}</div>
      </div>}
    </div>
  );
}
