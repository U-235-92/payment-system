package aq.project.utils.audit;

import aq.project.entities.audit.RevInfo;
import org.apache.shardingsphere.infra.algorithm.core.context.AlgorithmSQLContext;
import org.apache.shardingsphere.infra.algorithm.keygen.snowflake.SnowflakeKeyGenerateAlgorithm;
import org.hibernate.envers.RevisionListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ServiceRevisionListener implements RevisionListener {

    @Value("${application.services.wallet-service.audit.db}")
    private String db;
    @Value("${application.services.wallet-service.audit.schema}")
    private String schema;
    @Value("${application.services.wallet-service.audit.table}")
    private String table;
    @Value("${application.services.wallet-service.audit.column}")
    private String column;

    private final SnowflakeKeyGenerateAlgorithm snowflakeKeyGen = new SnowflakeKeyGenerateAlgorithm();

    @Override
    public void newRevision(Object revisionEntity) {
        RevInfo revInfo = (RevInfo) revisionEntity;
        final int keyGenerateCount = 1;
        AlgorithmSQLContext context = new AlgorithmSQLContext(db, schema, table, column);
        long rev = snowflakeKeyGen.generateKeys(context, keyGenerateCount)
                .stream()
                .findAny()
                .orElseThrow(() -> new RuntimeException("Failed generating a key for table revinfo"));
        revInfo.setRev(rev);
    }
}
