
import { useEffect, useState } from 'react';
import { api } from '../api.js';
import { subscribe } from '../ws.js';
import { inr } from '../places.js';
import Ranking from './Ranking.jsx';

export default function Admin() {
  const [stats, setStats] = useState(null);
  const [rides, setRides] = useState([]);
  const [users, setUsers] = useState([]);
  const [driverList, setDriverList] = useState([]);
  const [sos, setSos] = useState([]);
  const [tickets, setTickets] = useState([]);
  const [promos, setPromos] = useState([]);
  const [pf, setPf] = useState({
    code: '',
    percentOff: 10,
    maxDiscount: 100,
  });
  const [perr, setPerr] = useState('');
  const [loadErrors, setLoadErrors] = useState([]);

  useEffect(() => {
    let mounted = true;

    const load = async () => {
      const requests = [
        ['/api/admin/stats', setStats],
        ['/api/admin/rides', setRides],
        ['/api/admin/users', setUsers],
        ['/api/admin/drivers', setDriverList],
        ['/api/admin/tickets', setTickets],
        ['/api/admin/promos', setPromos],
      ];

      const results = await Promise.all(
        requests.map(async ([url, setter]) => {
          try {
            const data = await api(url);
            if (mounted) setter(data);
            return null;
          } catch (error) {
            return `${url}: ${error.message}`;
          }
        })
      );

      if (mounted) {
        setLoadErrors(results.filter(Boolean));
      }
    };

    load();

    const timer = setInterval(load, 5000);
    const unsubscribe = subscribe('/topic/admin/sos', (message) => {
      setSos((current) => [message, ...current]);
    });

    return () => {
      mounted = false;
      clearInterval(timer);
      unsubscribe();
    };
  }, []);

  const close = async (id) => {
    try {
      await api(`/api/admin/tickets/${id}/close`, {
        method: 'POST',
      });

      setTickets((current) =>
        current.map((ticket) =>
          ticket.id === id
            ? { ...ticket, status: 'CLOSED' }
            : ticket
        )
      );
    } catch (error) {
      setPerr(error.message);
    }
  };

  const reload = () =>
    api('/api/admin/promos').then(setPromos);

  const toggle = (code) =>
    api(`/api/admin/promos/${code}/toggle`, {
      method: 'POST',
    })
      .then(reload)
      .catch((error) => setPerr(error.message));

  const addPromo = (event) => {
    event.preventDefault();
    setPerr('');

    api('/api/admin/promos', {
      method: 'POST',
      body: {
        code: pf.code,
        percentOff: Number(pf.percentOff),
        maxDiscount: Number(pf.maxDiscount),
        active: true,
      },
    })
      .then(() => {
        setPf({
          code: '',
          percentOff: 10,
          maxDiscount: 100,
        });
        return reload();
      })
      .catch((error) => setPerr(error.message));
  };

  const formatDate = (value) =>
    value ? new Date(value).toLocaleString() : '—';

  const StatCard = ({ label, value }) => (
    <div className="card">
      <div className="text-sm text-mut">{label}</div>
      <div className="font-display text-2xl font-extrabold">
        {value ?? '—'}
      </div>
    </div>
  );

  return (
    <div className="mx-auto max-w-6xl">
      <h2 className="text-2xl font-extrabold">Admin console</h2>

      {sos.map((message, index) => (
        <div
          key={`${message.rideId}-${index}`}
          className="mt-3 rounded-xl bg-bad p-3 font-semibold text-white"
          role="alert"
        >
          SOS · ride #{message.rideId} · {message.message}
          {Number.isFinite(message.lat) &&
            Number.isFinite(message.lng) &&
            ` · ${message.lat.toFixed(4)}, ${message.lng.toFixed(4)}`}
        </div>
      ))}

      {loadErrors.length > 0 && (
        <div className="card mt-4 border border-line">
          <p className="font-semibold text-bad">
            Some admin data could not be loaded.
          </p>
          <ul className="mt-2 list-disc pl-5 text-sm">
            {loadErrors.map((error) => (
              <li key={error}>{error}</li>
            ))}
          </ul>
        </div>
      )}

      <div className="mt-4 grid grid-cols-2 gap-3 md:grid-cols-3 lg:grid-cols-4">
        <StatCard
          label="Registered users"
          value={stats?.registeredUsers}
        />
        <StatCard
          label="Registered drivers"
          value={stats?.registeredDrivers}
        />
        <StatCard
          label="Drivers online"
          value={stats?.driversOnline}
        />
        <StatCard
          label="Available rides"
          value={stats?.availableRides}
        />
        <StatCard label="Total rides" value={stats?.rides} />
        <StatCard
          label="Active rides"
          value={stats?.activeRides}
        />
        <StatCard
          label="Revenue"
          value={stats ? inr(stats.revenue) : null}
        />
      </div>

      <h3 className="mt-6 text-lg font-bold">Registered users</h3>
      <div className="card mt-2 overflow-x-auto">
        <table className="w-full text-sm">
          <thead>
            <tr className="text-left text-mut">
              <th className="p-2">ID</th>
              <th className="p-2">Name</th>
              <th className="p-2">Email</th>
              <th className="p-2">Role</th>
              <th className="p-2">Registered</th>
            </tr>
          </thead>
          <tbody>
            {users.map((user) => (
              <tr
                key={user.id}
                className="border-t border-line"
              >
                <td className="p-2">#{user.id}</td>
                <td className="p-2">{user.name}</td>
                <td className="p-2">{user.email}</td>
                <td className="p-2">{user.role}</td>
                <td className="whitespace-nowrap p-2">
                  {formatDate(user.createdAt)}
                </td>
              </tr>
            ))}
            {users.length === 0 && (
              <tr>
                <td colSpan={5} className="p-3 text-mut">
                  No registered users found.
                </td>
              </tr>
            )}
          </tbody>
        </table>
      </div>

      <h3 className="mt-6 text-lg font-bold">Registered drivers</h3>
      <div className="card mt-2 overflow-x-auto">
        <table className="w-full text-sm">
          <thead>
            <tr className="text-left text-mut">
              <th className="p-2">ID</th>
              <th className="p-2">Driver</th>
              <th className="p-2">Email</th>
              <th className="p-2">Vehicle</th>
              <th className="p-2">Plate</th>
              <th className="p-2">Availability</th>
              <th className="p-2">Rating</th>
            </tr>
          </thead>
          <tbody>
            {driverList.map((driver) => (
              <tr
                key={driver.id}
                className="border-t border-line"
              >
                <td className="p-2">#{driver.id}</td>
                <td className="p-2">{driver.name}</td>
                <td className="p-2">{driver.email}</td>
                <td className="p-2">{driver.vehicle || '—'}</td>
                <td className="p-2">{driver.plate || '—'}</td>
                <td className="p-2">
                  <span
                    className={
                      driver.online
                        ? 'font-semibold text-ok'
                        : 'text-mut'
                    }
                  >
                    {driver.online ? 'Online' : 'Offline'}
                  </span>
                </td>
                <td className="p-2">
                  {driver.ratingAvg ?? '—'} (
                  {driver.ratingCount ?? 0})
                </td>
              </tr>
            ))}
            {driverList.length === 0 && (
              <tr>
                <td colSpan={7} className="p-3 text-mut">
                  No registered drivers found.
                </td>
              </tr>
            )}
          </tbody>
        </table>
      </div>

      <h3 className="mt-6 text-lg font-bold">
        Recent rides
      </h3>
      <div className="card mt-2 overflow-x-auto">
        <table className="w-full text-sm">
          <thead>
            <tr className="text-left text-mut">
              <th className="p-2">ID</th>
              <th className="p-2">Route</th>
              <th className="p-2">Driver</th>
              <th className="p-2">Fare</th>
              <th className="p-2">Status</th>
            </tr>
          </thead>
          <tbody>
            {rides.map((ride) => (
              <tr
                key={ride.id}
                className="border-t border-line"
              >
                <td className="p-2">#{ride.id}</td>
                <td className="whitespace-nowrap p-2">
                  {ride.pickupName} → {ride.dropName}
                </td>
                <td className="p-2">
                  {ride.driverName ?? '—'}
                </td>
                <td className="p-2">{inr(ride.fare)}</td>
                <td className="p-2">{ride.status}</td>
              </tr>
            ))}
            {rides.length === 0 && (
              <tr>
                <td colSpan={5} className="p-3 text-mut">
                  No rides found.
                </td>
              </tr>
            )}
          </tbody>
        </table>
      </div>

      <h3 className="mt-6 text-lg font-bold">Driver ranking</h3>
      <Ranking embedded />

      <h3 className="mt-6 text-lg font-bold">Promo codes</h3>
      <div className="card mt-2">
        <div className="space-y-2">
          {promos.map((promo) => (
            <div
              key={promo.code}
              className="flex items-center justify-between gap-3"
            >
              <span>
                <b>{promo.code}</b> · {promo.percentOff}% off,
                up to {inr(promo.maxDiscount)}
              </span>
              <button
                onClick={() => toggle(promo.code)}
                className={`rounded-full border px-3 py-1 text-sm font-semibold ${
                  promo.active
                    ? 'border-ok text-ok'
                    : 'border-line text-mut'
                }`}
              >
                {promo.active ? 'Active' : 'Paused'}
              </button>
            </div>
          ))}
          {promos.length === 0 && (
            <p className="text-mut">No promo codes.</p>
          )}
        </div>

        <form
          onSubmit={addPromo}
          className="mt-4 grid grid-cols-1 items-end gap-3 sm:grid-cols-[1fr_90px_110px_auto]"
        >
          <div>
            <label className="lbl !mt-0" htmlFor="pc">
              Code
            </label>
            <input
              id="pc"
              className="input"
              required
              pattern="[A-Za-z0-9]{3,20}"
              value={pf.code}
              onChange={(event) =>
                setPf({ ...pf, code: event.target.value })
              }
            />
          </div>
          <div>
            <label className="lbl !mt-0" htmlFor="pp">
              % off
            </label>
            <input
              id="pp"
              type="number"
              min="1"
              max="90"
              className="input"
              value={pf.percentOff}
              onChange={(event) =>
                setPf({
                  ...pf,
                  percentOff: event.target.value,
                })
              }
            />
          </div>
          <div>
            <label className="lbl !mt-0" htmlFor="pm">
              Max ₹
            </label>
            <input
              id="pm"
              type="number"
              min="0"
              className="input"
              value={pf.maxDiscount}
              onChange={(event) =>
                setPf({
                  ...pf,
                  maxDiscount: event.target.value,
                })
              }
            />
          </div>
          <button
            className="rounded-xl bg-brand px-4 py-2.5 font-semibold text-onbrand"
            type="submit"
          >
            Add
          </button>
        </form>

        {perr && (
          <p className="mt-2 text-sm font-semibold text-bad" role="alert">
            {perr}
          </p>
        )}
      </div>

      <h3 className="mt-6 text-lg font-bold">Support tickets</h3>
      <div className="mt-2 space-y-2">
        {tickets.length === 0 && (
          <p className="text-mut">No tickets.</p>
        )}
        {tickets.map((ticket) => (
          <div
            key={ticket.id}
            className="card flex items-start justify-between gap-3 !py-3"
          >
            <div>
              <b>
                #{ticket.id} · {ticket.subject}
              </b>
              <p className="text-sm text-mut">{ticket.user}</p>
              <p className="mt-1 text-sm">{ticket.body}</p>
            </div>
            {ticket.status === 'OPEN' ? (
              <button
                className="rounded-full border border-line px-3 py-1 text-sm font-semibold"
                onClick={() => close(ticket.id)}
              >
                Close
              </button>
            ) : (
              <span className="text-sm font-semibold text-mut">
                Closed
              </span>
            )}
          </div>
        ))}
      </div>
    </div>
  );
}
