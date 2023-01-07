package br.ce.wcaquino.matchers;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;

import br.ce.wcaquino.utils.DataUtils;

public class DataDiferencaDiasMatcher extends TypeSafeMatcher<Date>{

    private Integer qtdDias;

    public DataDiferencaDiasMatcher(Integer qtdDias){
        this.qtdDias = qtdDias;
    }

    public void describeTo(Description description) {
        Calendar data = Calendar.getInstance();
        data.setTime(DataUtils.obterDataComDiferencaDias(qtdDias));
        String dataExtensao = data.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, new Locale("pt", "BR"));
        description.appendText(dataExtensao);
    }

    @Override
    protected boolean matchesSafely(Date data) {
        return DataUtils.isMesmaData(data, DataUtils.obterDataComDiferencaDias(qtdDias));
    }
    
}