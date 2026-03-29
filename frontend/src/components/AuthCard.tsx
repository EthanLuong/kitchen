import { getTokenLogin } from "../api/fetchFood";

type AuthenticationModalProps = {
  isOpen: boolean;
  setToken: (token: string | null) => void;
};

export default function AuthenticationModal({
  isOpen,
  setToken,
}: AuthenticationModalProps) {
  async function handleSubmit(e: React.SubmitEvent<HTMLFormElement>) {
    e.preventDefault();
    const data = new FormData(e.currentTarget);
    const username = data.get("username") as string;
    const password = data.get("password") as string;
    try {
      const token = await getTokenLogin(username, password);
      localStorage.setItem("token", token);
      setToken(token);
    } catch (err) {
      setToken(null);
      console.log(err);
    }
  }

  if (!isOpen) {
    return null;
  }

  return (
    <div className="overlay">
      <div className="authcard">
        <h2 className="auth-title">Kitchen</h2>
        <form onSubmit={handleSubmit} className="authform">
          <div className="formitem">
            <label>Username</label>
            <input type="text" name="username" />
          </div>
          <div className="formitem">
            <label>Password</label>
            <input type="password" name="password" />
          </div>
          <button className="auth-submit" type="submit">
            Login
          </button>
        </form>
      </div>
    </div>
  );
}
