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
        <div className="authcard-brand">
          <span className="authcard-mark" aria-hidden="true">
            <svg
              width="28"
              height="28"
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
              strokeWidth="1.5"
              strokeLinecap="round"
              strokeLinejoin="round"
            >
              <path d="M4 13h16a6 6 0 0 1-6 6h-4a6 6 0 0 1-6-6z" />
              <path d="M7 13V9a5 5 0 0 1 10 0v4" />
              <path d="M12 4v2" />
            </svg>
          </span>
          <h2 className="auth-title">Kitchen</h2>
          <p className="auth-tagline">
            Track what's in your fridge, pantry, and freezer.
          </p>
          <div className="auth-divider" aria-hidden="true" />
        </div>
        <form onSubmit={handleSubmit} className="authform">
          <div className="formitem">
            <label htmlFor="auth-username">Username</label>
            <input
              id="auth-username"
              type="text"
              name="username"
              autoComplete="username"
            />
          </div>
          <div className="formitem">
            <label htmlFor="auth-password">Password</label>
            <input
              id="auth-password"
              type="password"
              name="password"
              autoComplete={mode ? "current-password" : "new-password"}
            />
          </div>
          {!mode && (
            <div className="formitem">
              <label htmlFor="auth-confirm">Confirm password</label>
              <input
                id="auth-confirm"
                type="password"
                name="confirm"
                autoComplete="new-password"
              />
            </div>
          )}
          <button className="auth-submit" type="submit" disabled={isLoading}>
            {isLoading ? "Loading…" : mode ? "Log in" : "Create account"}
          </button>
        </form>
        {errorMessage && <p className="form-error">{errorMessage}</p>}
        <button
          type="button"
          className="auth-toggle"
          onClick={() => {
            setMode((prev) => !prev);
            setError(null);
          }}
        >
          {mode
            ? "New here? Create an account"
            : "Already have an account? Log in"}
        </button>
      </div>
    </div>
  );
}
