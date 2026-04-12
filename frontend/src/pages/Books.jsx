import { useState, useEffect } from 'react'
import { books } from '../api'

export default function Books({ session }) {
  const [bookList, setBookList] = useState([])
  const [search, setSearch] = useState('')
  const [showAdd, setShowAdd] = useState(false)
  const [form, setForm] = useState({ title: '', author: '', isbn: '', genre: '', copies: 1 })
  const [error, setError] = useState('')
  const [msg, setMsg] = useState('')

  useEffect(() => { loadBooks() }, [])

  async function loadBooks() {
    try {
      const data = await books.list()
      setBookList(data)
    } catch (err) {
      setError(err.message)
    }
  }

  async function handleSearch(e) {
    e.preventDefault()
    setError('')
    try {
      const data = search.trim() ? await books.search(search) : await books.list()
      setBookList(data)
    } catch (err) {
      setError(err.message)
    }
  }

  async function handleAdd(e) {
    e.preventDefault()
    setError('')
    setMsg('')
    try {
      await books.add({ ...form, copies: Number(form.copies) })
      setMsg('Book added successfully!')
      setForm({ title: '', author: '', isbn: '', genre: '', copies: 1 })
      setShowAdd(false)
      loadBooks()
    } catch (err) {
      setError(err.message)
    }
  }

  async function handleDelete(id) {
    if (!confirm('Remove this book?')) return
    try {
      await books.remove(id)
      loadBooks()
    } catch (err) {
      setError(err.message)
    }
  }

  return (
    <div className="page">
      <div className="page-header">
        <h2>Books</h2>
        {session.role === 'LIBRARIAN' && (
          <button className="btn btn-primary" onClick={() => setShowAdd(!showAdd)}>
            {showAdd ? 'Cancel' : '+ Add Book'}
          </button>
        )}
      </div>

      {error && <div className="error-msg">{error}</div>}
      {msg && <div className="success-msg">{msg}</div>}

      {showAdd && (
        <form className="card form-card" onSubmit={handleAdd}>
          <h3>Add New Book</h3>
          <div className="form-row">
            <div className="form-group">
              <label>Title</label>
              <input value={form.title} onChange={e => setForm({...form, title: e.target.value})} required />
            </div>
            <div className="form-group">
              <label>Author</label>
              <input value={form.author} onChange={e => setForm({...form, author: e.target.value})} required />
            </div>
          </div>
          <div className="form-row">
            <div className="form-group">
              <label>ISBN</label>
              <input value={form.isbn} onChange={e => setForm({...form, isbn: e.target.value})} required />
            </div>
            <div className="form-group">
              <label>Genre</label>
              <input value={form.genre} onChange={e => setForm({...form, genre: e.target.value})} />
            </div>
            <div className="form-group">
              <label>Copies</label>
              <input type="number" min="1" value={form.copies} onChange={e => setForm({...form, copies: e.target.value})} required />
            </div>
          </div>
          <button type="submit" className="btn btn-primary">Add Book</button>
        </form>
      )}

      <form className="search-bar" onSubmit={handleSearch}>
        <input
          type="text"
          placeholder="Search by title, author, or ISBN..."
          value={search}
          onChange={e => setSearch(e.target.value)}
        />
        <button type="submit" className="btn btn-secondary">Search</button>
      </form>

      <div className="table-container">
        <table>
          <thead>
            <tr>
              <th>ID</th>
              <th>Title</th>
              <th>Author</th>
              <th>ISBN</th>
              <th>Genre</th>
              <th>Available</th>
              {session.role === 'LIBRARIAN' && <th>Actions</th>}
            </tr>
          </thead>
          <tbody>
            {bookList.length === 0 ? (
              <tr><td colSpan={session.role === 'LIBRARIAN' ? 7 : 6} className="empty">No books found</td></tr>
            ) : (
              bookList.map(book => (
                <tr key={book.id}>
                  <td>{book.id}</td>
                  <td>{book.title}</td>
                  <td>{book.author}</td>
                  <td><code>{book.isbn}</code></td>
                  <td>{book.genre}</td>
                  <td>
                    <span className={`badge ${book.availableCopies > 0 ? 'badge-green' : 'badge-red'}`}>
                      {book.availableCopies}/{book.totalCopies}
                    </span>
                  </td>
                  {session.role === 'LIBRARIAN' && (
                    <td>
                      <button className="btn btn-danger btn-sm" onClick={() => handleDelete(book.id)}>Remove</button>
                    </td>
                  )}
                </tr>
              ))
            )}
          </tbody>
        </table>
      </div>
    </div>
  )
}
