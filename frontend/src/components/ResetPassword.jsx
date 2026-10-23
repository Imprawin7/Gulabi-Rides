import { useState } from 'react';
import { api } from '../api.js';

/** Shown at /reset?token=... (the link in the reset email). */
export default function ResetPassword() {
  const token = new URLSearchParams(window.location.search).get('token') || '';
  const [pw, setPw] = useState(''), [pw2, setPw2] = useState('');
  const [err, setErr] = useState(''), [done, setDone] = useState(false), [busy, setBusy] = useState(false);

  async function submit(e) {
    e.preventDefault(); setErr('');
    if (pw !== pw2) { setErr('The two passwords do not match'); return; }
    setBusy(true);
    try { await api('/api/auth/reset', { method: 'POST', body: { token, password: pw } }); setDone(true); }
    catch (x) { setErr(x.message); } finally { setBusy(false); }
  }

  return (
    <div className="grid min-h-screen place-items-center p-4">
      <form onSubmit={submit} className="card w-full max-w-sm">
        <div className="font-display text-3xl font-extrabold">Gulabi<span className="text-pop">.</span>rides</div>
        {done ? <>
          <p className="mt-3 font-semibold" role="status">Password updated. You can log in now.</p>
          <button type="button" className="btn mt-4" onClick={() => window.location.assign('/')}>Go to log in</button>
        </> : !token ? <p className="mt-3 text-bad">This reset link is incomplete. Request a new one from the login page.</p> : <>
          <p className="mt-1 text-mut">Choose a new password.</p>
          <label className="lbl" htmlFor="pw">New password</label>
          <input id="pw" type="password" minLength={8} required className="input" value={pw} onChange={(e) => setPw(e.target.value)} />
          <label className="lbl" htmlFor="pw2">Repeat password</label>
          <input id="pw2" type="password" minLength={8} required className="input" value={pw2} onChange={(e) => setPw2(e.target.value)} />
          {err && <p className="mt-3 text-sm font-semibold text-bad" role="alert">{err}</p>}
          <button className="btn mt-5" disabled={busy}>{busy ? 'Please wait…' : 'Update password'}</button>
        </>}
      </form>
    </div>
  );
}
