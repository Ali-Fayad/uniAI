-- Complete campus coordinates verified against official campus addresses and
-- public map/geolocation records.  Keep this correction separate from V57,
-- which may already have been applied in deployed databases.

-- Al Maaref University: Airport Avenue, Beirut.
-- Official address: https://www.mu.edu.lb/contact-us
-- Coordinate reference: https://lebanon.worldplaces.me/review/96586789-al-maaref-university.html
UPDATE campus c
SET latitude = 33.85642000,
    longitude = 35.50019000,
    updated_at = NOW()
FROM university u
WHERE c.university_id = u.id
  AND u.acronym = 'MU'
  AND c.name = 'Main Campus'
  AND c.latitude IS NULL
  AND c.longitude IS NULL;

-- Beirut Islamic University: Ibn Rushd Street, south of Dar Al-Fatwa, Beirut.
-- Official address: https://biu.edu.lb/pages/contact
-- Coordinate reference: https://www.wikidata.org/wiki/Q30294756
UPDATE campus c
SET latitude = 33.88752000,
    longitude = 35.48856370,
    updated_at = NOW()
FROM university u
WHERE c.university_id = u.id
  AND u.acronym = 'BIU'
  AND c.name = 'Main Campus'
  AND c.latitude IS NULL
  AND c.longitude IS NULL;

-- American University of Culture & Education: Badaro, Tayouneh Roundabout.
-- Official address: https://auce.edu.lb/page/3/
-- Coordinate reference: https://lb.near-place.com/auce-american-university-of-culture-education-tayouneh-omar-beyhum-st-badaro-lebanon-beirut-14
UPDATE campus c
SET latitude = 33.87198300,
    longitude = 35.51397000,
    updated_at = NOW()
FROM university u
WHERE c.university_id = u.id
  AND u.acronym = 'AUCE'
  AND c.name = 'Badaro Campus'
  AND c.latitude IS NULL
  AND c.longitude IS NULL;

-- American University of Technology: Halat-Fidar Highway, Byblos area.
-- Official campus location: https://www.aut.edu/wp-content/uploads/2025/06/AUT_Catalogue_2024-2025.pdf
-- Coordinate reference: https://lebanon.worldplaces.me/view-place/55134820-american-university-of-technology.html
UPDATE campus c
SET latitude = 34.09695000,
    longitude = 35.65420000,
    updated_at = NOW()
FROM university u
WHERE c.university_id = u.id
  AND u.acronym = 'AUOT'
  AND c.name = 'Main Campus'
  AND c.latitude IS NULL
  AND c.longitude IS NULL;

-- Notre Dame University-Louaize: Shouf Campus, Deir El Qamar.
-- Official campus location: https://www.ndu.edu.lb/our-campuses/shouf-campus
-- Coordinate reference: https://mapcarta.com/N7935858666
UPDATE campus c
SET latitude = 33.70274000,
    longitude = 35.56989000,
    updated_at = NOW()
FROM university u
WHERE c.university_id = u.id
  AND u.acronym = 'NDU'
  AND c.name = 'Shouf Campus'
  AND c.latitude IS NULL
  AND c.longitude IS NULL;
