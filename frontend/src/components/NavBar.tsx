type NavBarProps = {
  setModal: (mode: "add") => void;
  handleLogout: () => void;
};

export default function NavBar({ setModal, handleLogout }: NavBarProps) {
  return (
    <header className="navbar">
      <h1 className="navbar-title">Kitchen</h1>
      <div className="navbar-actions">
        <button onClick={() => setModal("add")}>+ Add Item</button>
        <button onClick={handleLogout}>Logout</button>
      </div>
    </header>
  );
}
