const KEY = 'gulabi.session';
export const session = {
  get: () => { try { return JSON.parse(localStorage.getItem(KEY)); } catch { return null; } },
  set: (v) => localStorage.setItem(KEY, JSON.stringify(v)),
  clear: () => localStorage.removeItem(KEY),
};

export async function api(path, { method = 'GET', body } = {}) {
  const s = session.get();
  const res = await fetch(path, {
    method,
    headers: { 'Content-Type': 'application/json', ...(s ? { Authorization: `Bearer ${s.token}` } : {}) },
    body: body ? JSON.stringify(body) : undefined,
  });
  if (res.status === 401 && s) { session.clear(); location.reload(); }
  const text = await res.text();
  const data = text ? JSON.parse(text) : null;
  if (!res.ok) throw new Error(data?.message || data?.error || `Request failed (${res.status})`);
  return data;
}

export async function downloadReceipt(id) {
  const res = await fetch(`/api/rides/${id}/receipt`, { headers: { Authorization: `Bearer ${session.get().token}` } });
  if (!res.ok) throw new Error('Could not download the receipt');
  const url = URL.createObjectURL(await res.blob());
  const a = document.createElement('a'); a.href = url; a.download = `receipt-${id}.pdf`; a.click(); URL.revokeObjectURL(url);
}
