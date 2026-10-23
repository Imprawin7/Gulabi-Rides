import { useEffect, useState } from 'react';
import { api, downloadReceipt } from '../api.js';
import { STATUS, inr } from '../places.js';

export default function History({ user }) {
  const [rides, setRides] = useState(null), [err, setErr] = useState('');
  const isDriver = user.role === 'DRIVER';
  useEffect(() => { api(isDriver ? '/api/driver/history' : '/api/rides/mine').then(setRides).catch((e) => setErr(e.message)); }, [isDriver]);

  return (
    <div className="mx-auto max-w-2xl">
      <h2 className="text-2xl font-extrabold">{isDriver ? 'Completed trips' : 'Your trips'}</h2>
      {err && <p className="mt-3 font-semibold text-bad" role="alert">{err}</p>}
      {rides === null && !err && <p className="mt-3 text-mut">Loading…</p>}
      {rides?.length === 0 && <p className="mt-3 text-mut">No trips yet.</p>}
      <div className="mt-4 space-y-3">
        {rides?.map((r) => (
          <div key={r.id} className="card">
            <div className="flex items-start justify-between gap-3">
              <div>
                <b>{r.pickupName} to {r.dropName}</b>
                <p className="text-sm text-mut">{r.createdAt && new Date(r.createdAt).toLocaleString('en-IN', { dateStyle: 'medium', timeStyle: 'short' })} · {r.distanceKm} km{!isDriver && r.driverName ? ` · ${r.driverName}` : ''}</p>
              </div>
              <div className="text-right"><b>{inr(r.fare)}</b><p className="text-sm text-mut">{STATUS[r.status]}</p></div>
            </div>
            {!isDriver && r.status === 'PAID' && <button className="btn-ghost mt-3 !py-2" onClick={() => downloadReceipt(r.id).catch((e) => setErr(e.message))}>Download receipt</button>}
          </div>
        ))}
      </div>
    </div>
  );
}
