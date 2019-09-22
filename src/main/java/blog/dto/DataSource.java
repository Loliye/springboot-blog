package blog.dto;

public class DataSource
{
    private String url;
    private String userName;
    private String password;
    private String driverClassName;
    private String dbName;

    @Override
    public String toString()
    {
        return "DataSource{" + "url='" + url + '\'' + ", userName='" + userName + '\'' + ", password='" + password + '\'' + ", driverClassName='" + driverClassName + '\'' + ", dbName='" + dbName + '\'' + '}';
    }

    public String getUrl()
    {
        return url;
    }

    public void setUrl(String url)
    {
        this.url = url;
    }

    public String getUserName()
    {
        return userName;
    }

    public void setUserName(String userName)
    {
        this.userName = userName;
    }

    public String getPassword()
    {
        return password;
    }

    public void setPassword(String password)
    {
        this.password = password;
    }

    public String getDriverClassName()
    {
        return driverClassName;
    }

    public void setDriverClassName(String driverClassName)
    {
        this.driverClassName = driverClassName;
    }

    public String getDbName()
    {
        return dbName;
    }

    public void setDbName(String dbName)
    {
        this.dbName = dbName;
    }
}
