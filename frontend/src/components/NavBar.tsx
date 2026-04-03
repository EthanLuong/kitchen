type NavBarProps = {
  setModal: (mode: "add") => void;
  handleLogout: () => void;
  setSettingModal: (mode: boolean) => void;
};

export default function NavBar({
  setModal,
  handleLogout,
  setSettingModal,
}: NavBarProps) {
  return (
    <header className="navbar">
      <h1 className="navbar-title">Kitchen</h1>
      <div className="navbar-actions">
        <button onClick={() => setModal("add")}>+ Add Item</button>
        <button onClick={handleLogout}>Logout</button>
        <button onClick={() => setSettingModal(true)}>Settings</button>
      </div>
    </header>
  );
}
