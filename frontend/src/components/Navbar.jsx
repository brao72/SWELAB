import { NavLink } from 'react-router-dom'
import Notifications from './Notifications'

export default function Navbar({ session, onLogout }) {
  return (
    <nav className="navbar">
      <div className="nav-brand">
        <span className="nav-logo">LibraTrack</span>
        <span className="nav-role">{session.role === 'LIBRARIAN' ? 'Librarian' : 'Member'}</span>
      </div>
      <div className="nav-links">
        <NavLink to="/books">Books</NavLink>
        {session.role === 'LIBRARIAN' && <NavLink to="/members">Members</NavLink>}
        <NavLink to="/borrow">Borrow</NavLink>
        <NavLink to="/fines">Fines</NavLink>
      </div>
      <div className="nav-user">
        {session.role === 'MEMBER' && <Notifications memberId={session.userId} />}
        <span>{session.displayName}</span>
        <button onClick={onLogout} className="btn btn-outline btn-sm">Logout</button>
      </div>
    </nav>
  )
}
