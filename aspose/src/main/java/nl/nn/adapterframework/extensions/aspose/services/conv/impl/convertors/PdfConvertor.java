/*
   Copyright 2019, 2021-2022 WeAreFrank!

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/
package nl.nn.adapterframework.extensions.aspose.services.conv.impl.convertors;

import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nonnull;

import org.springframework.http.MediaType;

import com.aspose.pdf.Document;
import com.aspose.pdf.LoadOptions;
import com.aspose.pdf.SaveFormat;
import com.aspose.pdf.XpsLoadOptions;
import com.aspose.pdf.exceptions.InvalidPasswordException;

import nl.nn.adapterframework.extensions.aspose.services.conv.CisConfiguration;
import nl.nn.adapterframework.extensions.aspose.services.conv.CisConversionResult;
import nl.nn.adapterframework.stream.Message;

/**
 * Converts the files which are required and supported by the Aspose pdf library.
 *
 * @author Gerard van der Hoorn
 */
public class PdfConvertor extends AbstractConvertor {

	private static final Map<MediaType, Class<? extends LoadOptions>> MEDIA_TYPE_LOAD_FORMAT_MAPPING;

	static {
		Map<MediaType, Class<? extends LoadOptions>> map = new HashMap<>();

		map.put(new MediaType("application", "vnd.ms-xpsdocument"), XpsLoadOptions.class);
		map.put(new MediaType("application", "x-tika-ooxml"), XpsLoadOptions.class);

		MEDIA_TYPE_LOAD_FORMAT_MAPPING = Collections.unmodifiableMap(map);
	}

	protected PdfConvertor(CisConfiguration configuration) {
		super(configuration, MEDIA_TYPE_LOAD_FORMAT_MAPPING.keySet());
	}

	@Override
	public void convert(MediaType mediaType, Message message, CisConversionResult result, String charset) throws Exception {
		if (!MEDIA_TYPE_LOAD_FORMAT_MAPPING.containsKey(mediaType)) {
			throw new IllegalArgumentException("Unsupported mediaType " + mediaType + " should never happen here!");
		}

		try (InputStream inputStream = message.asInputStream(charset)) {
			Document doc = new Document(inputStream, getLoadOptions(mediaType));
			doc.save(result.getPdfResultFile().getAbsolutePath(), SaveFormat.Pdf);
			doc.freeMemory();
			doc.close();
		}
		result.setNumberOfPages(getNumberOfPages(result.getPdfResultFile()));
	}

	@Nonnull
	private static LoadOptions getLoadOptions(final MediaType mediaType) throws InstantiationException, IllegalAccessException {
		return MEDIA_TYPE_LOAD_FORMAT_MAPPING.get(mediaType).newInstance();
	}

	@Override
	protected boolean isPasswordException(Exception e) {
		return e instanceof InvalidPasswordException;
	}

}
