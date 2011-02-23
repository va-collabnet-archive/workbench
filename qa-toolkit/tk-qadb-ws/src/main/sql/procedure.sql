CREATE DEFINER=`root`@`%` PROCEDURE `qa_controller`(in prun_id varchar(36))
begin

  set @not_clear='fe459506-0878-11e0-93c2-4c61feda47d3';
  set @clear='2d0cbf84-03dc-11e0-aa1f-b9ab2ee1e2c6';

update qa_rule inner join qa_rule_imp q on q.rule_uid=qa_rule.rule_uid
        set qa_rule.package_url= q.package_url,
        qa_rule.package_name= q.package_name, qa_rule.name=q.name,
        qa_rule.severity_uid=q.severity_uid,
        qa_rule.description=q.description,
        qa_rule.dita_documentation_link_uid=q.dita_documentation_link_uid,
        qa_rule.effective_time=CURRENT_TIMESTAMP,
        qa_rule.rule_code=q.rule_code
        where (ifnull(q.package_url,'')<> ifnull(qa_rule.package_url,'') or
        ifnull(q.package_name,'')<> ifnull(qa_rule.package_name,'') 
        or ifnull(q.name,'')<>ifnull(qa_rule.name,'')
        or ifnull(q.severity_uid,'')<>ifnull(qa_rule.severity_uid,'')
        or ifnull(q.description,'')<>ifnull(qa_rule.description,'') or
        ifnull(q.dita_documentation_link_uid,'')<>ifnull(qa_rule.dita_documentation_link_uid,'')
        or ifnull(q.rule_code,'')<>ifnull(qa_rule.rule_code,''));

  insert into qa_rule
  (`rule_uid`,
  `effective_time`,
  `status`,
  `package_url`,
  `package_name`,
  `severity_uid`,
  `name`,
  `description`,
  `dita_documentation_link_uid`)
  select tr.rule_uid,
  current_timestamp,
  1,
  tr.package_url,
  tr.package_name,
  tr.severity_uid,
  tr.name,
  tr.description,
  tr.dita_documentation_link_uid
  from qa_rule_imp tr where not exists
        (select 0 from qa_rule r where r.rule_uid =tr.rule_uid);

  update qa_component set status=1,effective_time=CURRENT_TIMESTAMP
    where status=0 and exists (select 0 from
        qa_finding f where f.qa_component_uid=qa_component.component_uid);

  insert into qa_component
  (`component_uid`,
  `status`,
  `name`)
  select distinct
  f.qa_component_uid,
  1,
  f.qa_component_name
  from qa_finding f
    where not exists (select 0 from
        qa_component c where c.component_uid= f.qa_component_uid);
  
  insert into qa_component
  (`component_uid`,
  `status`,
  `name`)
  select 
  r.path_uid,
  1,
  r.path_name
  from qa_run r
    where r.run_id=prun_id and not exists (select 0 from
        qa_component c where c.component_uid= r.path_uid);

  insert into qa_run_rules (rule_uid,run_id)
          select distinct rule_uid,prun_id from qa_rule_imp;
  
  update qa_case set status=1,effective_time=current_timestamp, last_status_change=current_timestamp
          where exists(select 0 from qa_finding where
              rule_uid=qa_case.rule_uid and database_uid=qa_case.database_uid and
              path_uid=qa_case.path_uid and qa_component_uid=qa_case.qa_component)
              and status=0 and disposition_status_uid<>@clear;

  update qa_case set status=1,effective_time=current_timestamp, last_status_change=current_timestamp
          where exists(select 0 from qa_finding f where
              f.rule_uid=qa_case.rule_uid and f.database_uid=qa_case.database_uid and
              f.path_uid=qa_case.path_uid and f.qa_component_uid=qa_case.qa_component)
              and qa_case.status=0 and qa_case.disposition_status_uid=@clear
              and exists(select 0 from qa_rule r where r.rule_uid=qa_case.rule_uid
              and r.is_whitelist_reset_when_closed=1);
  
  update qa_case set status=0,effective_time=current_timestamp, last_status_change=current_timestamp
           where not exists(select 0 from qa_finding f where
              f.rule_uid=qa_case.rule_uid and f.database_uid=qa_case.database_uid and
              f.path_uid=qa_case.path_uid and f.qa_component_uid=qa_case.qa_component)
            and qa_case.status=1
            and exists (
            select 0
              from qa_run r inner join qa_run_rules rr on rr.run_id=r.run_id
                where rr.rule_uid=qa_case.rule_uid and r.database_uid=qa_case.database_uid and
                r.path_uid=qa_case.path_uid and r.run_id = prun_id);

  insert into qa_case
  (`effective_time`,
  `status`,
  `rule_uid`,
  `database_uid`,
  `path_uid`,
  `qa_component`,
  `detail`,
  `disposition_status_uid`,
  `disposition_status_date`,
  `last_status_change`)
  select 
      f.effective_time,
      1,
      f.rule_uid,
      f.database_uid,
      f.path_uid,
      f.qa_component_uid,
      f.detail,
      @not_clear,
      current_timestamp,
      current_timestamp
    from qa_finding f where not exists(select 0 from qa_case c where
                c.rule_uid=f.rule_uid and c.database_uid=f.database_uid and
                c.path_uid=f.path_uid and c.qa_component=f.qa_component_uid);

end//