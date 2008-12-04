package org.dwfa.mojo.relformat.mojo.sql.io.util;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;
import org.junit.Before;
import org.junit.Test;

public final class FileUtilTest {

    private FileUtil fileUtil;

    @Before
    public void setup() {
        fileUtil = new FileUtilImpl();
    }

    @Test
    public void shouldReturnChangedExtension() {
        String result = fileUtil.changeExtension("aaa/bbb/eee/xyz.abc.def", "jkl");
        assertThat(result, equalTo("aaa/bbb/eee/xyz.abc.jkl"));
    }
}
