import { useEffect, useRef, useState } from 'react';
import { api } from '../api.js';

export default function Auth({ onAuth }) {
  const [mode, setMode] = useState('login');
  const [f, setF] = useState({ name: '', email: '', password: '', role: 'RIDER', vehicle: '', plate: '' });
  const [err, setErr] = useState(''), [info, setInfo] = useState('');
  const [busy, setBusy] = useState(false);
  const fRef = useRef(f); fRef.current = f;
  const gBtn = useRef(null);
  const gid = import.meta.env.VITE_GOOGLE_CLIENT_ID;

  // Render Google's button once their script has loaded; the callback reads the latest form values via fRef.
  useEffect(() => {
    if (!gid) return;
    const init = () => {
      if (!window.google?.accounts?.id || !gBtn.current) return false;
      window.google.accounts.id.initialize({ client_id: gid, callback: async ({ credential }) => {
        setErr('');
        try { const { role, vehicle, plate } = fRef.current; onAuth(await api('/api/auth/google', { method: 'POST', body: { idToken: credential, role, vehicle, plate } })); }
        catch (x) { setErr(x.message); }
      } });
      window.google.accounts.id.renderButton(gBtn.current, { theme: 'outline', size: 'large', width: 320, text: 'continue_with' });
      return true;
    };
    if (init()) return;
    const t = setInterval(() => { if (init()) clearInterval(t); }, 300);
    return () => clearInterval(t);
  }, [gid, mode]); // re-render Google's button when the form switches modes
  const set = (k) => (e) => setF({ ...f, [k]: e.target.value });

  async function submit(e) {
    e.preventDefault(); setErr(''); setInfo(''); setBusy(true);
    try {
      if (mode === 'forgot') {
        const r = await api('/api/auth/forgot', { method: 'POST', body: { email: f.email } });
        setInfo(r.message); return;
      }
      const body = mode === 'login' ? { email: f.email, password: f.password } : f;
      onAuth(await api(`/api/auth/${mode}`, { method: 'POST', body }));
    } catch (x) { setErr(x.message); } finally { setBusy(false); }
  }

  return (
    <div className="grid min-h-screen place-items-center p-4">
      <form onSubmit={submit} className="card w-full max-w-sm">
        <div className="font-display text-3xl font-extrabold">Gulabi<span className="text-pop">.</span>rides</div>
        <p className="mt-1 text-mut">{mode === 'login' ? 'Welcome back. Log in to ride or drive.' : mode === 'forgot' ? 'Enter your email and we will send you a reset link.' : 'Create your account.'}</p>
        {mode === 'register' && <>
          <label className="lbl" htmlFor="name">Full name</label>
          <input id="name" className="input" required value={f.name} onChange={set('name')} />
          <label className="lbl" htmlFor="role">I want to</label>
          <select id="role" className="input" value={f.role} onChange={set('role')}>
            <option value="RIDER">Book rides</option><option value="DRIVER">Drive and earn</option>
          </select>
          {f.role === 'DRIVER' && <>
            <label className="lbl" htmlFor="vehicle">Vehicle</label>
            <input id="vehicle" className="input" required value={f.vehicle} onChange={set('vehicle')} placeholder="Swift Dzire" />
            <label className="lbl" htmlFor="plate">Number plate</label>
            <input id="plate" className="input" required value={f.plate} onChange={set('plate')} placeholder="RJ14 CA 0000" />
          </>}
        </>}
        <label className="lbl" htmlFor="email">Email</label>
        <input id="email" type="email" className="input" required value={f.email} onChange={set('email')} />
        {mode !== 'forgot' && <>
          <label className="lbl" htmlFor="password">Password</label>
          <input id="password" type="password" minLength={8} className="input" required value={f.password} onChange={set('password')} />
        </>}
        {mode === 'login' && <button type="button" className="mt-2 text-sm font-semibold text-mut underline" onClick={() => { setErr(''); setInfo(''); setMode('forgot'); }}>Forgot your password?</button>}
        {info && <p className="mt-3 text-sm font-semibold" role="status">{info}</p>}
        {err && <p className="mt-3 text-sm font-semibold text-bad" role="alert">{err}</p>}
        <button className="btn mt-5" disabled={busy}>{busy ? 'Please wait…' : mode === 'login' ? 'Log in' : mode === 'forgot' ? 'Send reset link' : 'Create account'}</button>
        {gid && mode !== 'forgot' && <><div className="my-4 text-center text-sm text-mut">or</div><div ref={gBtn} className="flex justify-center" />
          {mode === 'login' && <p className="mt-2 text-center text-xs text-mut">New to Gulabi? Google creates a rider account. To drive, choose "Create an account" first.</p>}</>}
        <button type="button" className="btn-ghost mt-3" onClick={() => { setErr(''); setInfo(''); setMode(mode === 'register' ? 'login' : mode === 'forgot' ? 'login' : 'register'); }}>
          {mode === 'login' ? 'New here? Create an account' : mode === 'forgot' ? 'Back to log in' : 'Have an account? Log in'}
        </button>
      </form>
    </div>
  );
}
