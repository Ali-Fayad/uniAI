export function dedupeCitations(citations = []) {
  const seen = new Set();
  return citations.filter((citation) => {
    if (!citation || !citation.label || seen.has(citation.label)) {
      return false;
    }
    seen.add(citation.label);
    return true;
  });
}

export function buildCitationRows(citations = []) {
  return dedupeCitations(citations)
    .map((citation) => ({
      label: citation.label,
      title: citation.title || citation.label,
      url: citation.url || "",
    }))
    .filter((citation) => Boolean(citation.title) && Boolean(citation.url));
}
