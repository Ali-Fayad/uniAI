/**
 * universities.ts
 *
 * Static data for Lebanese universities shown on the Map page.
 * Extracted from useMap.ts to satisfy SRP: hooks contain behaviour,
 * data files contain static datasets.
 */

export type University = {
  id: number;
  name: string;
  coordinates: [number, number];
  description: string;
  website: string;
  color?: string;
};

const COLOR_PALETTE: string[] = [
  '#1f77b4', // blue
  '#ff7f0e', // orange
  '#2ca02c', // green
  '#d62728', // red
  '#9467bd', // purple
  '#8c564b', // brown
  '#e377c2', // pink
  '#7f7f7f', // gray
  '#bcbd22', // olive
  '#17becf', // cyan
];

const RAW_UNIVERSITIES: Omit<University, 'color'>[] = [
  {
    id: 1,
    name: 'American University of Beirut (AUB)',
    coordinates: [33.899, 35.4807],
    description:
      'Founded in 1866, AUB is a leading institution of higher learning in Lebanon and the Middle East.',
    website: 'https://www.aub.edu.lb',
  },
  {
    id: 2,
    name: 'Lebanese American University (LAU)',
    coordinates: [33.8201, 35.5352],
    description: 'A leading non-profit institution with campuses in Beirut and Byblos.',
    website: 'https://www.lau.edu.lb',
  },
  {
    id: 3,
    name: 'Université Saint-Joseph (USJ)',
    coordinates: [33.8835, 35.5059],
    description: 'A private French-language university founded in 1875.',
    website: 'https://www.usj.edu.lb',
  },
  {
    id: 4,
    name: 'Beirut Arab University (BAU)',
    coordinates: [33.8607, 35.5078],
    description: 'An Arab university offering programs in Arabic, English, and French.',
    website: 'https://www.bau.edu.lb',
  },
  {
    id: 5,
    name: 'Lebanese University (UL)',
    coordinates: [33.8719, 35.5138],
    description: 'The only public university in Lebanon, founded in 1951.',
    website: 'https://www.ul.edu.lb',
  },
  {
    id: 6,
    name: 'Notre Dame University (NDU)',
    coordinates: [34.0007, 35.6499],
    description: 'A Catholic institution with multiple campuses across Lebanon.',
    website: 'https://www.ndu.edu.lb',
  },
  {
    id: 7,
    name: 'Lebanese International University (LIU)',
    coordinates: [33.8422, 35.8394],
    description: 'A private university with multiple campuses across Lebanon.',
    website: 'https://www.liu.edu.lb',
  },
  {
    id: 8,
    name: 'Holy Spirit University of Kaslik (USEK)',
    coordinates: [33.978, 35.6477],
    description: 'A private Catholic university founded in 1938.',
    website: 'https://www.usek.edu.lb',
  },
  {
    id: 9,
    name: 'Haigazian University',
    coordinates: [33.88, 35.51],
    description:
      'A private university affiliated with the Union of Armenian Evangelical Churches.',
    website: 'https://www.haigazian.edu.lb',
  },
  {
    id: 10,
    name: 'Antonine University (UA)',
    coordinates: [34.0024, 35.6512],
    description: 'A private Catholic university run by the Antonine Maronite Order.',
    website: 'https://www.ua.edu.lb',
  },
];

/** Pre-built list with colour tokens applied. Import this in useMap. */
export const UNIVERSITIES: University[] = RAW_UNIVERSITIES.map((u, i) => ({
  ...u,
  color: COLOR_PALETTE[i % COLOR_PALETTE.length],
}));
