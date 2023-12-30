select name from kunden;
select id from kunden;

update kunden set name = "x" where name = "y"


update kunden set name = "y" where id = 2

   -- das ist ok 

select id as __id from kunden -- das ist ok
