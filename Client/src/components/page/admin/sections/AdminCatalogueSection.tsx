import { useEffect, useState } from 'react';
import type { FormEvent } from 'react';
import { adminService } from '../../../../services/admin';
import type { AdminCatalogItem } from '../../../../types/dto';
import AnimatedInput from '../../../common/AnimatedInput';
import FormButton from '../../../settings/FormButton';
import { isAxiosError } from 'axios';

type CatalogueKind = 'skills' | 'positions';

const normalize = (value: string) => value.trim().replace(/\s+/g, ' ');

const AdminCatalogueSection = () => {
  const [kind, setKind] = useState<CatalogueKind>('skills');
  const [query, setQuery] = useState('');
  const [items, setItems] = useState<AdminCatalogItem[]>([]);
  const [name, setName] = useState('');
  const [category, setCategory] = useState('');
  const [loading, setLoading] = useState(false);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  const load = async () => {
    setLoading(true);
    setError('');
    try {
      setItems(kind === 'skills' ? await adminService.searchSkills(query) : await adminService.searchPositions(query));
    } catch {
      setError('Unable to load catalogue values.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    const timeout = window.setTimeout(() => void load(), 300);
    return () => window.clearTimeout(timeout);
  }, [kind, query]);

  const submit = async (event: FormEvent) => {
    event.preventDefault();
    const normalized = normalize(name);
    if (!normalized || normalized.length > 255) {
      setError('Enter a name between 1 and 255 characters.');
      return;
    }
    setSaving(true);
    setError('');
    setSuccess('');
    try {
      if (kind === 'skills') await adminService.createSkill(normalized, normalize(category));
      else await adminService.createPosition(normalized);
      setName('');
      setCategory('');
      setSuccess(`${kind === 'skills' ? 'Skill' : 'Position'} added successfully.`);
      await load();
    } catch (requestError: unknown) {
      setError(isAxiosError(requestError) && requestError.response?.status === 409
        ? `${kind === 'skills' ? 'Skill' : 'Position'} already exists.`
        : 'Unable to add this catalogue value.');
    } finally {
      setSaving(false);
    }
  };

  return (
    <section id="admin-catalogue" className="space-y-5 rounded-2xl border border-[var(--color-border)] bg-[var(--color-surface)] p-5">
      <div className="flex flex-wrap gap-2" role="tablist" aria-label="Catalogue types">
        {(['skills', 'positions'] as CatalogueKind[]).map((value) => (
          <button key={value} type="button" role="tab" aria-selected={kind === value} onClick={() => setKind(value)}
            className={`rounded-full px-4 py-2 text-sm font-semibold ${kind === value ? 'bg-[var(--color-primary)] text-[var(--color-background)]' : 'bg-[var(--color-elevatedSurface)] text-[var(--color-textSecondary)]'}`}>
            {value === 'skills' ? 'Skills' : 'Positions'}
          </button>
        ))}
      </div>
      <div className="grid gap-5 lg:grid-cols-[1fr_20rem]">
        <div className="space-y-3">
          <AnimatedInput id="catalogue-search" label={`Search ${kind}`} value={query} onChange={(event) => setQuery(event.target.value)} />
          {loading ? <p className="text-sm text-[var(--color-textSecondary)]">Loading...</p> : null}
          {!loading && !items.length ? <p className="text-sm text-[var(--color-textSecondary)]">No matching values.</p> : null}
          <ul className="max-h-96 space-y-2 overflow-auto">
            {items.map((item) => <li key={item.id} className="rounded-xl border border-[var(--color-border)] px-3 py-2 text-sm text-[var(--color-textPrimary)]"><span className="font-medium">{item.name}</span>{item.category ? <span className="ml-2 text-[var(--color-textSecondary)]">{item.category}</span> : null}</li>)}
          </ul>
        </div>
        <form onSubmit={submit} className="space-y-3 rounded-xl bg-[var(--color-elevatedSurface)] p-4">
          <h3 className="font-semibold text-[var(--color-textPrimary)]">Add {kind === 'skills' ? 'skill' : 'position'}</h3>
          <AnimatedInput id="catalogue-name" label="Name" value={name} onChange={(event) => setName(event.target.value)} />
          {kind === 'skills' ? <AnimatedInput id="catalogue-category" label="Category (optional)" value={category} onChange={(event) => setCategory(event.target.value)} /> : null}
          <FormButton type="submit" disabled={saving}>{saving ? 'Adding...' : 'Add'}</FormButton>
          {success ? <p className="text-sm text-emerald-600" role="status">{success}</p> : null}
          {error ? <p className="text-sm text-red-500" role="alert">{error}</p> : null}
        </form>
      </div>
    </section>
  );
};

export default AdminCatalogueSection;
