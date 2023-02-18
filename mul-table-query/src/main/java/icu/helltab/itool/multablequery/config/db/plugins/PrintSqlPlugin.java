package icu.helltab.itool.multablequery.config.db.plugins;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.Properties;

import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.logging.jdbc.PreparedStatementLogger;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Plugin;
import org.apache.ibatis.plugin.Signature;
import org.apache.ibatis.session.ResultHandler;
import org.springframework.beans.factory.annotation.Value;

import cn.hutool.db.sql.SqlUtil;
import com.alibaba.druid.pool.DruidPooledPreparedStatement;
import com.baomidou.mybatisplus.extension.handlers.AbstractSqlParserHandler;
import com.mysql.cj.PreparedQuery;
import com.mysql.cj.jdbc.ClientPreparedStatement;
import lombok.extern.slf4j.Slf4j;


@Intercepts({
    @Signature(type = StatementHandler.class, method = "query", args = {Statement.class, ResultHandler.class}),
    @Signature(type = StatementHandler.class, method = "batch", args = {Statement.class}),
    @Signature(type = StatementHandler.class, method = "update", args = {Statement.class}),
})
@Slf4j
public class PrintSqlPlugin extends AbstractSqlParserHandler implements Interceptor {
    @Value("${custom.debug:true}")
    boolean debug;
    public PrintSqlPlugin() {
    }
    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        if(!debug) {
            return invocation.proceed();
        }
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
            System.out.println(statement1.getClass());
            ClientPreparedStatement rawStatement = (ClientPreparedStatement) statement1;
            StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
            StackTraceElement stackTraceElement = null;
            for (StackTraceElement element : stackTrace) {
                if (element.getClassName().matches("icu\\.helltab\\.fpstore\\..*?\\.controller.*")) {
                    stackTraceElement = element;
                    break;
                }
            }

            log.debug(( "\n\n------------------------------------------\n" +
                    "SQL_L: {}\n" +
                    "SQL_I: {}" +
                    "\n------------------------------------------\n\n")
                , stackTraceElement, SqlUtil.formatSql(((PreparedQuery)rawStatement.getQuery()).asSql()));
        } catch (Exception e) {
            log.debug( "db-error: {}", e.getMessage());
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
