import { useState } from "react";
import { getTokenLogin, createNewUser } from "../api/fetchFood";

type AuthenticationModalProps = {
  mode: boolean;
  setMode: React.Dispatch<React.SetStateAction<boolean>>;
  setToken: (token: string | null) => void;
};

export default function AuthenticationModal({
  mode,
  setMode,
  setToken,
}: AuthenticationModalProps) {
  const [isLoading, setIsLoading] = useState(false);
  const [errorMessage, setError] = useState<null | string>(null);
  async function handleSubmit(e: React.SubmitEvent<HTMLFormElement>) {
    e.preventDefault();
    const data = new FormData(e.currentTarget);
    const username = data.get("username") as string;
    const password = data.get("password") as string;
    const confirm = !mode ? (data.get("confirm") as string) : "";
    try {
      setIsLoading(true);
      setError(null);
      if (mode) {
        const token = await getTokenLogin(username, password);
        localStorage.setItem("token", token);
        setToken(token);
      } else {
        if (password != confirm) {
          throw new Error("Passwords don't match");
        } else {
          await createNewUser(username, password);
          setMode(true);
        }
      }
    } catch (err) {
      setToken(null);
      setError(err instanceof Error ? err.message : "Something went wrong");
    } finally {
      setIsLoading(false);
    }
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
          {!mode && (
            <div className="formitem">
              <label>Confirm Password</label>
              <input type="password" name="confirm" />
            </div>
          )}
          <button className="auth-submit" type="submit" disabled={isLoading}>
            {isLoading ? "Loading..." : mode ? "Login" : "Signup"}
          </button>
        </form>
        <a
          onClick={() => {
            setMode((prev) => !prev);
            setError(null);
          }}
        >
          {mode
            ? "Not registered? Create an account"
            : "Have an account? Login"}
        </a>
        {errorMessage && <p className="form-error">{errorMessage}</p>}
      </div>
    </div>
  );
}
