create trigger link_hit_tr01
    BEFORE INSERT OR UPDATE ON link_hit
    FOR EACH ROW
EXECUTE PROCEDURE trgf_audit ();