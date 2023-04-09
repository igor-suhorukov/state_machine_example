Final State Machine example for PostgreSQL on pure SQL inside "CREATE AGGREGATE"( generated from state-transition table).
```sql
SELECT life_alias.name,life_alias.age,life_alias.desire_to_learn,life_alias.exams, state_name.name state FROM 
            (SELECT *, fsm(hstore(ARRAY['st0_1', (age>=3 and desire_to_learn)::text , 
                                        'st1_2', (age>=7 and exams is null and desire_to_learn)::text , 
                                        'st2_3', (age>=11 and exams is null and desire_to_learn)::text , 
                                        'st3_4', (age>=15 and exams='выпускные экзамены в 9 классе')::text , 
                                        'st4_5', (age>=15 and desire_to_learn)::text , 
                                        'st5_6', (age>=17 and exams='выпускные экзамены в 11 классе')::text , 
                                        'st4_7', (age>=16 and exams='вступительные экзамены в техникум' and desire_to_learn)::text , 
                                        'st7_8', (age>=18 and exams='защита диплома')::text , 
                                        'st6_9', (age>=18 and exams='вступительные экзамены в ВУЗ' and desire_to_learn)::text , 
                                        'st8_9', (age>=18 and exams='вступительные экзамены в ВУЗ' and desire_to_learn)::text , 
                                        'st9_10', (age>=22 and exams='защита диплома' and desire_to_learn)::text , 
                                        'st10_11', (age>=22  and exams='экзамены в аспирантуру' and desire_to_learn)::text , 
                                        'st11_12', (age>=24 and exams='кандидатский минимум' and desire_to_learn)::text , 
                                        'st12_13', (age>=25 and exams='защита диссертации' and desire_to_learn)::text , 
                                        'st4_14', (not(desire_to_learn))::text , 
                                        'st6_14', (not(desire_to_learn))::text , 
                                        'st8_14', (not(desire_to_learn))::text , 
                                        'st10_14', (not(desire_to_learn))::text , 
                                        'st13_14', (not(desire_to_learn))::text ]))
                                         OVER (PARTITION BY name ORDER BY age) FROM life ORDER BY name,age) life_alias 
INNER JOIN 
( VALUES (0, 'появился на свет'), (1, 'ходит в детсадик'), (2, 'ученик начальной школы'), (3, 'ученик средней школы'), 
         (4, 'основное общее образование'), (5, 'ученик старших классов'), (6, 'среднее общее образование'), 
         (7, 'учащийся техникума'), (8, 'среднее профессиональное образование'), (9, 'учащийся института'), 
         (10, 'оконченное высшее образование'), (11, 'аспирант'), (12, 'претендент на ученую степень'), 
         (13, 'кандидат наук'), (14, 'больше не обучается')) AS state_name(state, name) 
ON state_name.state=life_alias.fsm;
```

```bash
 name | age | desire_to_learn |             exams              |             state             
------+-----+-----------------+--------------------------------+-------------------------------
 Вася |   1 | t               |                                | появился на свет
 Вася |   2 | t               |                                | появился на свет
 Вася |   3 | t               |                                | ходит в детсадик
 Вася |   4 | t               |                                | ходит в детсадик
 Вася |   5 | t               |                                | ходит в детсадик
 Вася |   6 | t               |                                | ходит в детсадик
 Вася |   7 | t               |                                | ученик начальной школы
 Вася |   8 | t               |                                | ученик начальной школы
 Вася |   9 | t               |                                | ученик начальной школы
 Вася |  10 | t               |                                | ученик начальной школы
 Вася |  11 | t               |                                | ученик средней школы
 Вася |  12 | t               |                                | ученик средней школы
 Вася |  13 | t               |                                | ученик средней школы
 Вася |  14 | t               |                                | ученик средней школы
 Вася |  15 | t               | выпускные экзамены в 9 классе  | основное общее образование
 Вася |  16 | t               |                                | ученик старших классов
 Вася |  17 | t               | выпускные экзамены в 11 классе | среднее общее образование
 Вася |  18 | t               | вступительные экзамены в ВУЗ   | учащийся института
 Вася |  19 | t               |                                | учащийся института
 Вася |  20 | t               |                                | учащийся института
 Вася |  21 | t               |                                | учащийся института
 Вася |  22 | t               | защита диплома                 | оконченное высшее образование
 Вася |  23 | t               |                                | оконченное высшее образование
 Вася |  24 | t               |                                | оконченное высшее образование
 Вася |  25 | t               |                                | оконченное высшее образование
 Вася |  26 | t               |                                | оконченное высшее образование
 Вася |  27 | f               |                                | больше не обучается
 Вася |  28 | f               |                                | больше не обучается
 Вася |  29 | f               |                                | больше не обучается
 Вася |  30 | f               |                                | больше не обучается
(30 rows)
```