package icu.helltab.itool.multablequery.config.db.plugins;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Properties;

import com.alibaba.druid.proxy.jdbc.PreparedStatementProxyImpl;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.logging.jdbc.PreparedStatementLogger;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Plugin;
import org.apache.ibatis.plugin.Signature;
import org.apache.ibatis.session.ResultHandler;

import com.alibaba.druid.pool.DruidPooledPreparedStatement;
import com.baomidou.mybatisplus.extension.handlers.AbstractSqlParserHandler;
import lombok.extern.slf4j.Slf4j;


/**
 * @author Helltab
 * @mail helltab@163.com
 * @date 2023/4/18 13:53
 * @desc sql 打印
 * todo 这里对于最终 sql 的拼装还有问题
 */
@Intercepts({
    @Signature(type = StatementHandler.class, method = "query", args = {Statement.class, ResultHandler.class}),
    @Signature(type = StatementHandler.class, method = "batch", args = {Statement.class}),
    @Signature(type = StatementHandler.class, method = "update", args = {Statement.class}),
})
@Slf4j
public class PrintSqlPlugin extends AbstractSqlParserHandler implements Interceptor {
    public PrintSqlPlugin() {
    }
    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        try {
            DruidPooledPreparedStatement statement = null;
            Object[] args = invocation.getArgs();
            for (Object arg : args) {
                if (null == arg) continue;
                if (Proxy.isProxyClass(arg.getClass())) {
                    InvocationHandler invocationHandler = Proxy.getInvocationHandler(arg);
                    PreparedStatementLogger logger = (PreparedStatementLogger) invocationHandler;
                    PreparedStatement preparedStatement = logger.getPreparedStatement();
                    statement = (DruidPooledPreparedStatement) preparedStatement;
                    break;
                } else {

                    statement = (DruidPooledPreparedStatement) arg;
                    break;
                }
            }
            assert statement != null;
            PreparedStatement statement1 = statement.getRawStatement();
            PreparedStatementProxyImpl rawStatement = (PreparedStatementProxyImpl) statement1;
            StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
            StackTraceElement stackTraceElement = Arrays.stream(stackTrace).filter(element -> element.getClassName()
                    .matches(".*?\\.controller\\..*?Controller")).findFirst().orElse(null);
            log.info(( "\n\n------------------------------------------\n" +
                            "SQL_L: {}\n" +
                            "SQL_I: {}\n" +
                            "SQL_P: {}" +
                            "\n------------------------------------------\n\n")
                    , stackTraceElement, rawStatement.getLastExecuteSql(), rawStatement.getParameters());
        } catch (Exception e) {
            log.error( "db-print-error: {}", e.getMessage());
        }
        return invocation.proceed();
    }

    @Override
    public Object plugin(Object o) {
        return Plugin.wrap(o, this);
    }

    @Override
    public void setProperties(Properties properties) {

    }
}
