CREATE OR REPLACE FUNCTION fsm_transition(
  state smallint,
  transition transition_parameter
) RETURNS smallint AS $$
select CASE
 WHEN state=0 THEN --появился на свет
	CASE
		WHEN transition.st0_1 -- age>=3 and desire_to_learn
			THEN 1 --ходит в детсадик
		ELSE state
	END
 WHEN state=1 THEN --ходит в детсадик
	CASE
		WHEN transition.st1_2 -- age>=7 and exams is null and desire_to_learn
			THEN 2 --ученик начальной школы
		ELSE state
	END
 WHEN state=2 THEN --ученик начальной школы
	CASE
		WHEN transition.st2_3 -- age>=11 and exams is null and desire_to_learn
			THEN 3 --ученик средней школы
		ELSE state
	END
 WHEN state=3 THEN --ученик средней школы
	CASE
		WHEN transition.st3_4 -- age>=15 and exams='выпускные экзамены в 9 классе'
			THEN 4 --основное общее образование
		ELSE state
	END
 WHEN state=4 THEN --основное общее образование
	CASE
		WHEN transition.st4_5 -- age>=15 and desire_to_learn
			THEN 5 --ученик старших классов
		WHEN transition.st4_7 -- age>=16 and exams='вступительные экзамены в техникум' and desire_to_learn
			THEN 7 --учащийся техникума
		WHEN transition.st4_14 -- not(desire_to_learn)
			THEN 14 --больше не обучается
		ELSE state
	END
 WHEN state=5 THEN --ученик старших классов
	CASE
		WHEN transition.st5_6 -- age>=17 and exams='выпускные экзамены в 11 классе'
			THEN 6 --среднее общее образование
		ELSE state
	END
 WHEN state=6 THEN --среднее общее образование
	CASE
		WHEN transition.st6_9 -- age>=18 and exams='вступительные экзамены в ВУЗ' and desire_to_learn
			THEN 9 --учащийся института
		WHEN transition.st6_14 -- not(desire_to_learn)
			THEN 14 --больше не обучается
		ELSE state
	END
 WHEN state=7 THEN --учащийся техникума
	CASE
		WHEN transition.st7_8 -- age>=18 and exams='защита диплома'
			THEN 8 --среднее профессиональное образование
		ELSE state
	END
 WHEN state=8 THEN --среднее профессиональное образование
	CASE
		WHEN transition.st8_9 -- age>=18 and exams='вступительные экзамены в ВУЗ' and desire_to_learn
			THEN 9 --учащийся института
		WHEN transition.st8_14 -- not(desire_to_learn)
			THEN 14 --больше не обучается
		ELSE state
	END
 WHEN state=9 THEN --учащийся института
	CASE
		WHEN transition.st9_10 -- age>=22 and exams='защита диплома' and desire_to_learn
			THEN 10 --оконченное высшее образование
		ELSE state
	END
 WHEN state=10 THEN --оконченное высшее образование
	CASE
		WHEN transition.st10_11 -- age>=22  and exams='экзамены в аспирантуру' and desire_to_learn
			THEN 11 --аспирант
		WHEN transition.st10_14 -- not(desire_to_learn)
			THEN 14 --больше не обучается
		ELSE state
	END
 WHEN state=11 THEN --аспирант
	CASE
		WHEN transition.st11_12 -- age>=24 and exams='кандидатский минимум' and desire_to_learn
			THEN 12 --претендент на ученую степень
		ELSE state
	END
 WHEN state=12 THEN --претендент на ученую степень
	CASE
		WHEN transition.st12_13 -- age>=25 and exams='защита диссертации' and desire_to_learn
			THEN 13 --кандидат наук
		ELSE state
	END
 WHEN state=13 THEN --кандидат наук
	CASE
		WHEN transition.st13_14 -- not(desire_to_learn)
			THEN 14 --больше не обучается
		ELSE state
	END
	ELSE state
END
$$ LANGUAGE sql;

CREATE OR REPLACE FUNCTION fsm_final( state smallint) RETURNS smallint AS $$ select state $$ LANGUAGE sql;

CREATE OR REPLACE AGGREGATE fsm(transition transition_parameter) (
  sfunc     = fsm_transition,
  stype     = smallint,
  finalfunc = fsm_final,
  initcond  = '0'
);