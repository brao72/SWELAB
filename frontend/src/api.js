const BASE = '/api';

async function request(url, options = {}) {
  const res = await fetch(BASE + url, {
    headers: { 'Content-Type': 'application/json', ...options.headers },
    ...options,
  });
  if (res.status === 204) return null;
  const text = await res.text();
  if (!text) {
    if (!res.ok) throw new Error(`Request failed (${res.status})`);
    return null;
  }
  const data = JSON.parse(text);
  if (!res.ok) throw new Error(data.error || 'Request failed');
  return data;
}

export const auth = {
  loginLibrarian: (username, password) =>
    request('/auth/login', {
      method: 'POST',
      body: JSON.stringify({ type: 'librarian', username, password }),
    }),
  loginMember: (memberId, password) =>
    request('/auth/login', {
      method: 'POST',
      body: JSON.stringify({ type: 'member', memberId: Number(memberId), password }),
    }),
};

export const books = {
  list: () => request('/books'),
  search: (q) => request(`/books/search?q=${encodeURIComponent(q)}`),
  add: (book) =>
    request('/books', { method: 'POST', body: JSON.stringify(book) }),
  remove: (id) => request(`/books/${id}`, { method: 'DELETE' }),
};

export const members = {
  list: () => request('/members'),
  get: (id) => request(`/members/${id}`),
  register: (member) =>
    request('/members', { method: 'POST', body: JSON.stringify(member) }),
  deactivate: (id) =>
    request(`/members/${id}/deactivate`, { method: 'PATCH' }),
};

export const borrow = {
  issue: (memberId, isbn, dueDate) =>
    request('/borrow/issue', {
      method: 'POST',
      body: JSON.stringify({ memberId: Number(memberId), isbn, dueDate: dueDate || null }),
    }),
  returnBook: (memberId, isbn) =>
    request('/borrow/return', {
      method: 'POST',
      body: JSON.stringify({ memberId: Number(memberId), isbn }),
    }),
  reserve: (memberId, isbn) =>
    request('/borrow/reserve', {
      method: 'POST',
      body: JSON.stringify({ memberId: Number(memberId), isbn }),
    }),
  history: (memberId) => request(`/borrow/history/${memberId}`),
  notifications: (memberId) => request(`/borrow/notifications/${memberId}`),
  dismissNotification: (reservationId) => request(`/borrow/notifications/${reservationId}/dismiss`, { method: 'POST' }),
};

export const fines = {
  getUnpaid: (memberId) => request(`/fines/${memberId}`),
  getTotal: (memberId) => request(`/fines/${memberId}/total`),
  pay: (fineId, memberId) =>
    request(`/fines/${fineId}/pay`, {
      method: 'POST',
      body: JSON.stringify({ memberId: Number(memberId) }),
    }),
};
