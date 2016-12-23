/*
 * Copyright (C) 2016 Jorge Ruesga
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.ruesga.rview;

import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.ruesga.rview.gerrit.filter.ChangeQuery;
import com.ruesga.rview.gerrit.filter.antlr.QueryParseException;
import com.ruesga.rview.misc.ActivityHelper;
import com.ruesga.rview.misc.StringHelper;
import com.ruesga.rview.model.Account;
import com.ruesga.rview.preferences.Constants;
import com.ruesga.rview.preferences.Preferences;
import com.ruesga.rview.widget.RegExLinkifyTextView;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;

public class UrlHandlerProxyActivity extends AppCompatDelegateActivity {

    private static final String TAG = "UrlHandlerProxyActivity";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Check we have valid arguments
        if (getIntent() == null || getIntent().getData() == null) {
            finish();
            return;
        }

        // Check we have something we allow to handle
        Uri data = getIntent().getData();
        String scheme = data.getScheme();
        if (!scheme.equals("http") && !scheme.equals("https")) {
            finish();
            return;
        }

        // If we don't have an account, then we can handle the link for sure
        Account account = Preferences.getAccount(this);
        if (account == null) {
            openExternalHttpLinkAndFinish(data);
            return;
        }

        // Check that we have an activity account which can handle the request
        List<Account> accounts = Preferences.getAccounts(this);
        List<Account> targetAccounts = new ArrayList<>();
        String type = "";
        for (Account acct : accounts) {
            List<RegExLinkifyTextView.RegExLink> links =
                    RegExLinkifyTextView.createRepositoryRegExpLinks(acct.mRepository);
            for (RegExLinkifyTextView.RegExLink link : links) {
                if (link.mPattern.matcher(data.toString()).find()) {
                    targetAccounts.add(acct);

                    // We can assume safely that all matches are of the same type
                    type = link.mType;
                }
            }
        }

        // No accounts are able to handle the link
        if (targetAccounts.isEmpty()) {
            openExternalHttpLinkAndFinish(data);
            return;
        }

        // Should we change account
        boolean isSameAccount = false;
        for (Account acct : targetAccounts) {
            if (account.getAccountHash().equals(acct.getAccountHash())) {
                isSameAccount = true;
                break;
            }
        }
        if (!isSameAccount) {
            // Change to the first of account. TODO should we ask to the user?
            Preferences.setAccount(this, targetAccounts.get(0));
        }

        // Open the change details
        switch (type) {
            case Constants.CUSTOM_URI_CHANGE_ID:
                ActivityHelper.openChangeDetailsByUri(
                        this, ActivityHelper.createCustomUri(this, Constants.CUSTOM_URI_CHANGE_ID,
                                StringHelper.getSafeLastPathSegment(data)));
                break;

            case Constants.CUSTOM_URI_QUERY:
                String query = extractQuery(data);
                if (!TextUtils.isEmpty(query)) {
                    try {
                        ChangeQuery filter = ChangeQuery.parse(query);
                        ActivityHelper.openChangeListByFilterActivity(this, null, filter, true);
                        break;
                    } catch (QueryParseException ex) {
                        // Ignore
                        Log.w(TAG, "Can parse query: " + query);
                        Toast.makeText(this, getString(
                                R.string.exception_cannot_handle_link, data.toString()),
                                    Toast.LENGTH_SHORT).show();
                        break;
                    }
                }
                // fallback to default

            default:
                // We cannot handle this
                openExternalHttpLinkAndFinish(data);
                return;
        }
        finish();
    }

    private void openExternalHttpLinkAndFinish(Uri link) {
        String source = getIntent().getStringExtra(Constants.EXTRA_SOURCE);
        if (source != null && source.equals(getPackageName())) {
            ActivityHelper.openUriInCustomTabs(this, link, true);
        } else {
            ActivityHelper.openUri(this, link, true);
        }
        finish();
    }

    private String extractQuery(Uri uri) {
        String url = uri.toString();
        int pos = url.indexOf("/q/");
        if (pos == -1) {
            return null;
        }
        try {
            String query = url.substring(pos + 3);
            // Double url decode to ensure is all has the proper format to be parsed
            return URLDecoder.decode(URLDecoder.decode(query, "UTF-8"), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            // Ignore
        }
        return null;
    }
}
