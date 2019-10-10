SET foreign_key_checks = 0;

UPDATE application_finance af
    INNER JOIN growth_table g on g.temp_application_finance_id = af.id
SET af.growth_table_id = g.id;

SET foreign_key_checks = 1;