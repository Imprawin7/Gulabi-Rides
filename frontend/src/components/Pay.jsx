import { useEffect, useState } from 'react';
import { loadStripe } from '@stripe/stripe-js';
import { Elements, PaymentElement, useStripe, useElements } from '@stripe/react-stripe-js';
import { api } from '../api.js';
import { inr } from '../places.js';

const pk = import.meta.env.VITE_STRIPE_PK;
const stripePromise = pk ? loadStripe(pk) : null;

function Form({ amount }) {
  const stripe = useStripe(), elements = useElements();
  const [busy, setBusy] = useState(false), [msg, setMsg] = useState('');
  async function pay(e) {
    e.preventDefault(); setBusy(true); setMsg('');
    const { error } = await stripe.confirmPayment({ elements, redirect: 'if_required' });
    if (error) setMsg(error.message); else setMsg('Payment received. Confirming…');
    setBusy(false);
  }
  return (
    <form onSubmit={pay} className="mt-3">
      <PaymentElement />
      {msg && <p className="mt-2 text-sm text-mut" role="status">{msg}</p>}
      <button className="btn mt-4" disabled={!stripe || busy}>Pay {inr(amount)}</button>
    </form>
  );
}

export default function Pay({ ride }) {
  const [secret, setSecret] = useState(null), [err, setErr] = useState('');
  useEffect(() => {
    if (!stripePromise) return;
    api(`/api/payments/intent/${ride.id}`, { method: 'POST' }).then((r) => setSecret(r.clientSecret)).catch((e) => setErr(e.message));
  }, [ride.id]);

  if (!stripePromise) return <p className="mt-3 text-sm text-mut">Set VITE_STRIPE_PK in .env to enable card payments.</p>;
  if (err) return <p className="mt-3 text-sm font-semibold text-bad">{err}</p>;
  if (!secret) return <p className="mt-3 text-sm text-mut">Preparing secure payment…</p>;
  return <Elements stripe={stripePromise} options={{ clientSecret: secret }}><Form amount={ride.fare} /></Elements>;
}
