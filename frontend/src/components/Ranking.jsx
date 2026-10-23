import { useEffect, useState } from 'react';
import { api } from '../api.js';

export default function Ranking({ embedded }) {
  const [rows, setRows] = useState(null), [err, setErr] = useState('');
  useEffect(() => { api('/api/drivers/ranking').then(setRows).catch((e) => setErr(e.message)); }, []);
  return (
    <div className={embedded ? '' : 'mx-auto max-w-2xl'}>
      {!embedded && <h2 className="text-2xl font-extrabold">Driver ranking</h2>}
      {err && <p className="mt-3 font-semibold text-bad" role="alert">{err}</p>}
      {rows?.length === 0 && <p className="mt-3 text-mut">No drivers yet.</p>}
      {rows?.length > 0 && <div className="card mt-3 overflow-x-auto"><table className="w-full text-sm">
        <thead><tr className="text-left text-mut"><th className="p-2">#</th><th className="p-2">Driver</th><th className="p-2">Rating</th><th className="p-2">Trips</th></tr></thead>
        <tbody>{rows.map((r) => (
          <tr key={r.rank} className={`border-t border-line ${r.you ? 'bg-bg font-semibold' : ''}`}>
            <td className="p-2">{r.rank}</td>
            <td className="whitespace-nowrap p-2">{r.name}{r.you && ' (you)'}<div className="text-xs font-normal text-mut">{r.vehicle}</div></td>
            <td className="whitespace-nowrap p-2"><span className="text-pop">★</span> {r.rating} <span className="text-mut">({r.ratings})</span></td>
            <td className="p-2">{r.trips}</td></tr>))}</tbody>
      </table></div>}
      <p className="mt-3 text-xs text-mut">Ranked by a weighted rating, so a single 5-star review does not outrank a long record of good ones.</p>
    </div>
  );
}
