import type { CitationDto } from "../../types/dto";

export function dedupeCitations(citations?: CitationDto[]): CitationDto[];
export function buildCitationRows(citations?: CitationDto[]): Array<{
  label: string;
  title: string;
  url: string;
}>;
