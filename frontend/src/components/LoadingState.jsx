function LoadingState({ message = "Loading..." }) {
  return (
    <div className="rounded-3xl border border-ink-200 bg-white/80 p-10 text-center shadow-soft">
      <div className="mx-auto h-10 w-10 animate-spin rounded-full border-4 border-ink-200 border-t-brand-600" />
      <p className="mt-4 text-sm text-ink-500">{message}</p>
    </div>
  );
}

export default LoadingState;
