import type { CSSProperties, ReactNode } from 'react';
import type { CVTemplatePalette } from './templateTypes';
import type { ResumeEntry, TemplateSectionData } from './templateHelpers';

interface ResumeTheme {
  palette: CVTemplatePalette;
  fontFamily?: string;
}

interface ResumePageProps {
  children: ReactNode;
  theme: ResumeTheme;
  className?: string;
  style?: CSSProperties;
}

interface ResumeHeaderProps {
  name: string;
  title?: string | null;
  company?: string | null;
  summary?: string | null;
  contactItems: string[];
  theme: ResumeTheme;
  align?: 'left' | 'center';
  compact?: boolean;
}

interface ResumeSectionProps {
  section: TemplateSectionData;
  theme: ResumeTheme;
  variant?: 'standard' | 'ruled' | 'compact';
  children?: ReactNode;
}

interface ResumeItemProps {
  item: ResumeEntry;
  theme: ResumeTheme;
  compact?: boolean;
}

interface SkillListProps {
  items: ResumeEntry[];
  theme: ResumeTheme;
  variant?: 'pills' | 'plain' | 'bars';
}

export const ResumePage = ({ children, theme, className = '', style }: ResumePageProps) => (
  <article
    className={`mx-auto min-h-[277mm] w-full text-[13px] leading-[1.45] shadow-sm print:min-h-0 print:shadow-none ${className}`}
    style={{
      backgroundColor: theme.palette.paper,
      color: theme.palette.ink,
      fontFamily: theme.fontFamily ?? 'Inter, Arial, sans-serif',
      ...style,
    }}
  >
    {children}
  </article>
);

export const ResumeHeader = ({
  name,
  title,
  company,
  summary,
  contactItems,
  theme,
  align = 'left',
  compact = false,
}: ResumeHeaderProps) => (
  <header className={align === 'center' ? 'text-center' : undefined}>
    <h3
      className={compact ? 'text-[24px] font-semibold leading-tight' : 'text-[34px] font-semibold leading-tight'}
      style={{ color: theme.palette.ink, letterSpacing: 0 }}
    >
      {name}
    </h3>
    {(title || company) && (
      <p className="mt-1 text-[12px] font-medium uppercase" style={{ color: theme.palette.accent, letterSpacing: '0.12em' }}>
        {[title, company].filter(Boolean).join(' | ')}
      </p>
    )}
    {contactItems.length > 0 && (
      <p className="mt-2 text-[11px] leading-relaxed" style={{ color: theme.palette.muted }}>
        {contactItems.join('  |  ')}
      </p>
    )}
    {summary && (
      <p className={compact ? 'mt-3 text-[12px]' : 'mt-5 text-[13px]'} style={{ color: theme.palette.ink }}>
        {summary}
      </p>
    )}
  </header>
);

export const ResumeSection = ({ section, theme, variant = 'standard', children }: ResumeSectionProps) => (
  <section className={variant === 'compact' ? 'break-inside-avoid' : 'break-inside-avoid'}>
    <div
      className={variant === 'ruled' ? 'mb-2 border-b pb-1' : 'mb-2 flex items-center gap-2'}
      style={{ borderColor: theme.palette.rule }}
    >
      <h4
        className={variant === 'compact' ? 'text-[10px] font-bold uppercase' : 'text-[11px] font-bold uppercase'}
        style={{ color: theme.palette.accent, letterSpacing: '0.12em' }}
      >
        {section.title}
      </h4>
      {variant === 'standard' && <span className="h-px flex-1" style={{ backgroundColor: theme.palette.rule }} />}
    </div>
    {children ?? (
      <div className={variant === 'compact' ? 'space-y-2' : 'space-y-3'}>
        {section.items.map((item) => (
          <ResumeItem key={item.id} item={item} theme={theme} compact={variant === 'compact'} />
        ))}
      </div>
    )}
  </section>
);

export const ResumeItem = ({ item, theme, compact = false }: ResumeItemProps) => (
  <div className="break-inside-avoid">
    <div className="flex items-start justify-between gap-4">
      <div className="min-w-0">
        <p className={compact ? 'text-[12px] font-semibold' : 'text-[13px] font-semibold'} style={{ color: theme.palette.ink }}>
          {item.title}
        </p>
        {item.subtitle && (
          <p className="text-[12px]" style={{ color: theme.palette.muted }}>
            {item.subtitle}
          </p>
        )}
      </div>
      {item.meta && (
        <p className="shrink-0 text-right text-[11px]" style={{ color: theme.palette.muted }}>
          {item.meta}
        </p>
      )}
    </div>
    {item.description && (
      <p className="mt-1 text-[12px] leading-relaxed" style={{ color: theme.palette.ink }}>
        {item.description}
      </p>
    )}
    {item.tags && item.tags.length > 0 && (
      <div className="mt-2 flex flex-wrap gap-1.5">
        {item.tags.map((tag) => (
          <span key={tag} className="rounded-sm px-1.5 py-0.5 text-[10px]" style={{ backgroundColor: theme.palette.accentSoft, color: theme.palette.accent }}>
            {tag}
          </span>
        ))}
      </div>
    )}
  </div>
);

export const SkillList = ({ items, theme, variant = 'pills' }: SkillListProps) => {
  if (variant === 'plain') {
    return (
      <p className="text-[12px] leading-relaxed" style={{ color: theme.palette.ink }}>
        {items.map((item) => [item.title, item.meta].filter(Boolean).join(' - ')).join(' | ')}
      </p>
    );
  }

  if (variant === 'bars') {
    return (
      <div className="space-y-2">
        {items.map((item) => (
          <div key={item.id}>
            <div className="flex justify-between gap-3 text-[12px]">
              <span style={{ color: theme.palette.ink }}>{item.title}</span>
              {item.meta && <span style={{ color: theme.palette.muted }}>{item.meta}</span>}
            </div>
            <div className="mt-1 h-1.5" style={{ backgroundColor: theme.palette.accentSoft }}>
              <div className="h-full w-3/4" style={{ backgroundColor: theme.palette.accent }} />
            </div>
          </div>
        ))}
      </div>
    );
  }

  return (
    <div className="flex flex-wrap gap-1.5">
      {items.map((item) => (
        <span key={item.id} className="rounded-sm px-2 py-1 text-[11px] font-medium" style={{ backgroundColor: theme.palette.accentSoft, color: theme.palette.accent }}>
          {[item.title, item.meta].filter(Boolean).join(' - ')}
        </span>
      ))}
    </div>
  );
};
