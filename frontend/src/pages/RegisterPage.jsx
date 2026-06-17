import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { useAuth } from "../hooks/useAuth.jsx";

function RegisterPage() {
  const navigate = useNavigate();
  const { register } = useAuth();
  const [form, setForm] = useState({
    username: "",
    email: "",
    password: "",
    confirmPassword: "",
  });
  const [error, setError] = useState("");
  const [isSubmitting, setIsSubmitting] = useState(false);

  async function handleSubmit(event) {
    event.preventDefault();

    if (form.password !== form.confirmPassword) {
      setError("Passwords do not match.");
      return;
    }

    try {
      setIsSubmitting(true);
      setError("");
      await register({
        username: form.username,
        email: form.email,
        password: form.password,
      });
      navigate("/dashboard", { replace: true });
    } catch (requestError) {
      setError(requestError.userMessage || "Registration failed.");
    } finally {
      setIsSubmitting(false);
    }
  }

  return (
    <div className="flex min-h-screen items-center justify-center px-4 py-10 sm:px-6">
      <div className="grid w-full max-w-5xl overflow-hidden rounded-[2rem] border border-ink-200 bg-white shadow-soft lg:grid-cols-[1.1fr_0.9fr]">
        <section className="bg-ink-900 px-6 py-8 text-white sm:px-10 sm:py-12">
          <p className="text-sm uppercase tracking-[0.35em] text-brand-100">Fleet Management</p>
          <h1 className="mt-6 max-w-md text-4xl font-semibold leading-tight">
            Create the first operator account and enter the control panel.
          </h1>
          <p className="mt-4 max-w-lg text-sm leading-6 text-slate-300 sm:text-base">
            Registration is connected to the Spring Boot auth API and signs the user in
            immediately after success.
          </p>

          <div className="mt-10 grid gap-4 sm:grid-cols-2">
            <Feature title="Quick Setup" description="Create a user without leaving the app." />
            <Feature title="JWT Session" description="Receive a token and enter the dashboard instantly." />
            <Feature title="Responsive UI" description="Works on phone, tablet and desktop screens." />
            <Feature title="Backend Aligned" description="Uses the current register contract directly." />
          </div>
        </section>

        <section className="px-6 py-8 sm:px-10 sm:py-12">
          <div className="mx-auto w-full max-w-md">
            <p className="text-sm font-semibold uppercase tracking-[0.2em] text-brand-600">
              Create account
            </p>
            <h2 className="mt-3 text-3xl font-semibold text-ink-900">Register</h2>
            <p className="mt-2 text-sm text-ink-500">
              Use a valid email and a password with at least 6 characters.
            </p>

            <form onSubmit={handleSubmit} className="mt-8 space-y-5">
              <FormField
                id="username"
                label="Username"
                value={form.username}
                onChange={(value) => setForm((current) => ({ ...current, username: value }))}
                autoComplete="username"
              />
              <FormField
                id="email"
                label="Email"
                type="email"
                value={form.email}
                onChange={(value) => setForm((current) => ({ ...current, email: value }))}
                autoComplete="email"
              />
              <FormField
                id="password"
                label="Password"
                type="password"
                value={form.password}
                onChange={(value) => setForm((current) => ({ ...current, password: value }))}
                autoComplete="new-password"
              />
              <FormField
                id="confirmPassword"
                label="Confirm Password"
                type="password"
                value={form.confirmPassword}
                onChange={(value) => setForm((current) => ({ ...current, confirmPassword: value }))}
                autoComplete="new-password"
              />

              {error ? (
                <div className="rounded-2xl border border-rose-200 bg-rose-50 px-4 py-3 text-sm text-rose-700">
                  {error}
                </div>
              ) : null}

              <button
                type="submit"
                disabled={isSubmitting}
                className="w-full rounded-2xl bg-brand-600 px-4 py-3 text-sm font-semibold text-white transition hover:bg-brand-700 disabled:cursor-not-allowed disabled:opacity-70"
              >
                {isSubmitting ? "Creating account..." : "Create account"}
              </button>
            </form>

            <p className="mt-6 text-sm text-ink-500">
              Already have an account?{" "}
              <Link to="/login" className="font-semibold text-brand-700 hover:text-brand-800">
                Sign in
              </Link>
            </p>
          </div>
        </section>
      </div>
    </div>
  );
}

function Feature({ title, description }) {
  return (
    <div className="rounded-2xl border border-white/10 bg-white/5 p-4">
      <p className="font-semibold">{title}</p>
      <p className="mt-2 text-sm text-slate-300">{description}</p>
    </div>
  );
}

function FormField({ id, label, type = "text", value, onChange, autoComplete }) {
  return (
    <label className="block" htmlFor={id}>
      <span className="mb-2 block text-sm font-medium text-ink-700">{label}</span>
      <input
        id={id}
        type={type}
        value={value}
        autoComplete={autoComplete}
        onChange={(event) => onChange(event.target.value)}
        className="w-full rounded-2xl border border-ink-200 bg-white px-4 py-3 text-ink-900 outline-none transition placeholder:text-ink-400 focus:border-brand-500 focus:ring-4 focus:ring-brand-100"
        required
      />
    </label>
  );
}

export default RegisterPage;
