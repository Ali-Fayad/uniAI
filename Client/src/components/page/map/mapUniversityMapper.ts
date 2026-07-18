import type { CampusCatalogDto, UniversityCatalogDto } from '../../../types/dto';

export interface MapUniversity {
  id: number;
  universityId: number;
  campusId: number;
  name: string;
  nameAr?: string;
  acronym?: string;
  campusName: string;
  campusType?: string;
  city: string;
  locality?: string;
  coordinates: [number, number];
  color: string;
}

export const getUniversityColor = (universityId: number): string => {
  const hue = (universityId * 137.508) % 360;
  return `hsl(${hue.toFixed(0)} 65% 48%)`;
};

const hasValidCoordinates = (
  campus: CampusCatalogDto,
): campus is CampusCatalogDto & { latitude: number; longitude: number } => {
  const { latitude, longitude } = campus;
  return (
    typeof latitude === 'number' &&
    Number.isFinite(latitude) &&
    latitude >= -90 &&
    latitude <= 90 &&
    typeof longitude === 'number' &&
    Number.isFinite(longitude) &&
    longitude >= -180 &&
    longitude <= 180
  );
};

export const mapUniversityCatalog = (
  universities: UniversityCatalogDto[],
): MapUniversity[] =>
  universities.flatMap((university) =>
    (university.campuses ?? [])
      .filter(hasValidCoordinates)
      .map((campus) => ({
        id: campus.id,
        universityId: university.id,
        campusId: campus.id,
        name: university.name,
        nameAr: university.nameAr ?? undefined,
        acronym: university.acronym ?? undefined,
        campusName: campus.name,
        campusType: campus.campusType ?? undefined,
        city: campus.city,
        locality: campus.locality ?? undefined,
        coordinates: [campus.latitude, campus.longitude],
        color: getUniversityColor(university.id),
      })),
  );

export const formatLocation = (university: MapUniversity): string =>
  [university.locality, university.city]
    .filter(Boolean)
    .filter((value, index, values) => values.indexOf(value) === index)
    .join(', ');
