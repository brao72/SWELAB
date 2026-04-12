import { useState } from 'react'
import { Routes, Route, Navigate } from 'react-router-dom'
import Navbar from './components/Navbar'
import Login from './pages/Login'
import Books from './pages/Books'
import Members from './pages/Members'
import Borrow from './pages/Borrow'
import Fines from './pages/Fines'

export default function App() {
  const [session, setSession] = useState(() => {
    const saved = sessionStorage.getItem('session')
    return saved ? JSON.parse(saved) : null
  })

  function handleLogin(s) {
    sessionStorage.setItem('session', JSON.stringify(s))
    setSession(s)
  }

  function handleLogout() {
    sessionStorage.removeItem('session')
    setSession(null)
  }

  if (!session) {
    return <Login onLogin={handleLogin} />
  }

  return (
    <div className="app">
      <Navbar session={session} onLogout={handleLogout} />
      <main className="main-content">
        <Routes>
          <Route path="/books" element={<Books session={session} />} />
          {session.role === 'LIBRARIAN' && (
            <Route path="/members" element={<Members />} />
          )}
          <Route path="/borrow" element={<Borrow session={session} />} />
          <Route path="/fines" element={<Fines session={session} />} />
          <Route path="*" element={<Navigate to="/books" replace />} />
        </Routes>
      </main>
    </div>
  )
}
