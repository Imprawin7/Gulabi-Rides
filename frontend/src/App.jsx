import { useState } from 'react';
import { session } from './api.js';
import { connect, disconnect } from './ws.js';
import Auth from './components/Auth.jsx';
import Rider from './components/Rider.jsx';
import Driver from './components/Driver.jsx';
import Admin from './components/Admin.jsx';
import History from './components/History.jsx';
import Support from './components/Support.jsx';
import Ranking from './components/Ranking.jsx';
import ResetPassword from './components/ResetPassword.jsx';

const TABS = {
  RIDER: [['ride', 'Book a ride', Rider], ['trips', 'Trips', History], ['help', 'Help', Support]],
  DRIVER: [['drive', 'Drive', Driver], ['trips', 'Trips', History], ['ranking', 'Ranking', Ranking], ['help', 'Help', Support]],
  ADMIN: [['console', 'Console', Admin]],
};

export default function App() {
  const [user, setUser] = useState(() => { const s = session.get(); if (s) connect(s.token); return s; });
  const [tab, setTab] = useState(0);

  const onAuth = (s) => { session.set(s); connect(s.token); setTab(0); setUser(s); };
  const logout = () => { disconnect(); session.clear(); setUser(null); };

  if (!user && window.location.pathname === '/reset') return <ResetPassword />;
  if (!user) return <Auth onAuth={onAuth} />;
  const tabs = TABS[user.role];
  const View = tabs[tab][2];
  return (
    <>
      <header className="sticky top-0 z-[1000] border-b border-line bg-bg">
        <div className="mx-auto flex max-w-6xl flex-wrap items-center justify-between gap-2 px-4 py-3">
          <div className="font-display text-xl font-extrabold">Gulabi<span className="text-pop">.</span>rides</div>
          <div className="flex items-center gap-3 text-sm">
            <span className="hidden text-mut sm:inline">{user.name}</span>
            <button className="rounded-full border border-line px-3 py-1 font-semibold" onClick={logout}>Log out</button>
          </div>
        </div>
        {tabs.length > 1 && <nav className="mx-auto flex max-w-6xl gap-1 overflow-x-auto px-4 pb-2" aria-label="Sections">
          {tabs.map(([k, label], i) => (
            <button key={k} onClick={() => setTab(i)} aria-current={i === tab ? 'page' : undefined}
              className={`whitespace-nowrap rounded-full px-4 py-1.5 text-sm font-semibold ${i === tab ? 'bg-brand text-onbrand' : 'text-mut'}`}>{label}</button>))}
        </nav>}
      </header>
      <main className="mx-auto max-w-6xl p-4 pb-10 lg:p-6"><View key={user.role + tab} user={user} /></main>
    </>
  );
}
