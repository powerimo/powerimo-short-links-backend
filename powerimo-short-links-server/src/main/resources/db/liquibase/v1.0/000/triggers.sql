CREATE FUNCTION public.trgf_audit (
)
    RETURNS trigger
AS
$body$
BEGIN
    if NEW.created_at is null then
        NEW.created_at := now() at time zone 'UTC';
    end if;
    NEW.updated_at := now() at time zone 'UTC';
    return NEW;
END;
$body$
    LANGUAGE plpgsql;

create trigger link_tr01
    BEFORE INSERT OR UPDATE ON link
    FOR EACH ROW
EXECUTE PROCEDURE trgf_audit ();