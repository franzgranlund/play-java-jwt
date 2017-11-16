import jwt.filter.JwtFilter;
import play.Environment;
import play.api.http.EnabledFilters;
import play.filters.cors.CORSFilter;
import play.http.DefaultHttpFilters;
import play.mvc.EssentialFilter;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

public class Filters extends DefaultHttpFilters {
    private final Environment env;
    private final EssentialFilter jwtFilter;
    private final EnabledFilters enabledFilters;
    private final CORSFilter corsFilter;

    @Inject
    public Filters(Environment env, JwtFilter jwtFilter, EnabledFilters enabledFilters, CORSFilter corsFilter) {
        this.env = env;
        this.jwtFilter = jwtFilter;
        this.enabledFilters = enabledFilters;
        this.corsFilter = corsFilter;
    }

    private static List<EssentialFilter> combine(List<EssentialFilter> filters, EssentialFilter toAppend) {
        List<EssentialFilter> combinedFilters = new ArrayList<>(filters);
        combinedFilters.add(toAppend);
        return combinedFilters;
    }

    @Override
    public List<EssentialFilter> getFilters() {
        List<EssentialFilter> zeFilters = enabledFilters.asJava().getFilters();
        zeFilters.add(corsFilter.asJava());
        zeFilters.add(jwtFilter);
        return zeFilters;
    }
}