SELECT o.name_en, o.name_bn,
       round(GREATEST(similarity(o.name_en, :'q'), similarity(o.name_bn, :'q'),
         COALESCE((SELECT max(similarity(s.term, :'q')) FROM occupation_synonym s WHERE s.occupation_id = o.id), 0))::numeric, 2) AS score
FROM occupation o
WHERE o.name_en ILIKE '%' || :'q' || '%' OR o.name_bn ILIKE '%' || :'q' || '%'
   OR similarity(o.name_en, :'q') > 0.25 OR similarity(o.name_bn, :'q') > 0.25
   OR EXISTS (SELECT 1 FROM occupation_synonym s WHERE s.occupation_id = o.id
              AND (s.term ILIKE '%' || :'q' || '%' OR similarity(s.term, :'q') > 0.25))
ORDER BY (o.name_en ILIKE :'q' || '%' OR o.name_bn ILIKE :'q' || '%') DESC, score DESC, o.name_en
LIMIT 5;
