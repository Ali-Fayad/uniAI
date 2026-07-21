import { useEffect, useState } from 'react';
import { adminService } from '../../../../services/admin';
import type { AdminPromptResponse } from '../../../../types/dto';

const AdminPromptsSection = () => {
  const [prompts, setPrompts] = useState<AdminPromptResponse[]>([]);
  const [selected, setSelected] = useState<AdminPromptResponse | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    void (async () => {
      try {
        const result = await adminService.listPrompts();
        setPrompts(result);
        if (result[0]) setSelected(await adminService.getPrompt(result[0].key));
      } catch {
        setError('Unable to load AI prompts.');
      } finally {
        setLoading(false);
      }
    })();
  }, []);

  const openPrompt = async (key: string) => {
    try {
      setSelected(await adminService.getPrompt(key));
    } catch {
      setError('Unable to load this prompt.');
    }
  };

  if (loading) return <p className="rounded-2xl border border-[var(--color-border)] p-5 text-sm">Loading prompts...</p>;
  if (error) return <p className="rounded-2xl border border-red-300 p-5 text-sm text-red-500" role="alert">{error}</p>;

  return (
    <section className="grid gap-5 rounded-2xl border border-[var(--color-border)] bg-[var(--color-surface)] p-5 lg:grid-cols-[18rem_1fr]">
      <div className="space-y-2">
        {prompts.map((prompt) => <button key={prompt.key} type="button" onClick={() => void openPrompt(prompt.key)} className={`w-full rounded-xl border p-3 text-left ${selected?.key === prompt.key ? 'border-[var(--color-primary)] bg-[var(--color-elevatedSurface)]' : 'border-[var(--color-border)]'}`}><span className="block font-semibold text-[var(--color-textPrimary)]">{prompt.displayName}</span><span className="text-xs text-[var(--color-textSecondary)]">{prompt.riskLevel} · Read only</span></button>)}
      </div>
      {selected ? <article className="min-w-0 space-y-3"><div><h2 className="text-xl font-bold text-[var(--color-textPrimary)]">{selected.displayName}</h2><p className="text-sm text-[var(--color-textSecondary)]">{selected.description}</p></div><dl className="grid gap-2 text-sm sm:grid-cols-2"><div><dt className="font-semibold">Key</dt><dd>{selected.key}</dd></div><div><dt className="font-semibold">Caller</dt><dd>{selected.caller}</dd></div><div><dt className="font-semibold">Operation</dt><dd>{selected.operation}</dd></div><div><dt className="font-semibold">Expected output</dt><dd>{selected.expectedOutput}</dd></div><div><dt className="font-semibold">Resource</dt><dd>{selected.resourcePath}</dd></div><div><dt className="font-semibold">Status</dt><dd>Read only · {selected.riskLevel}</dd></div></dl><pre className="max-h-[36rem] overflow-auto whitespace-pre-wrap rounded-xl bg-[var(--color-elevatedSurface)] p-4 font-mono text-xs leading-5 text-[var(--color-textPrimary)]">{selected.content}</pre></article> : <p className="text-sm text-[var(--color-textSecondary)]">Select a prompt to inspect it.</p>}
    </section>
  );
};

export default AdminPromptsSection;
