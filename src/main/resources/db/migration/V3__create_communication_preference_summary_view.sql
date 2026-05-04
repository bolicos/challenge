CREATE OR REPLACE VIEW vw_communication_preference_summary AS
SELECT
    cp.id,
    cp.customer_id,
    cp.communication_channel,
    COUNT(e.id) AS email_count,
    cp.created_at AS data_criacao,
    cp.last_modified_at AS data_atualizacao
FROM communication_preferences cp
LEFT JOIN emails e ON e.preference_id = cp.id
GROUP BY
    cp.id,
    cp.customer_id,
    cp.communication_channel,
    cp.created_at,
    cp.last_modified_at;
