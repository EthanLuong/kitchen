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
      <form onSubmit={handleSubmit} className="login">
        <div>
          Username: <input type="text" name="username"></input>
        </div>
        <div>
          Password: <input type="password" name="password"></input>
        </div>
        <button type="submit">Login</button>
      </form>
    </div>
  );
}
