package com.example.pets;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NavUtils;

import android.app.AlertDialog;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Loader;

import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import android.app.LoaderManager;

import com.example.pets.database.PetContract.PetEntry;

public class EditorActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final int EXISTING_PET_LOADER = 0;

    private EditText nameEditText;
    private EditText breedEditText;
    private Spinner genderSpinner;
    private EditText weightEditText;

    private Uri currentPetUri;
    private int gender = PetEntry.GENDER_UNKNOWN;

    private boolean petHasChanged = false;
    private View.OnTouchListener touchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            petHasChanged = true;
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        currentPetUri = getIntent().getData();
        if(currentPetUri == null) {
            setTitle(getString(R.string.editor_activity_title_new_pet));

            invalidateOptionsMenu();
        } else {
            setTitle(getString(R.string.editor_activity_edit_pet));

            getLoaderManager().initLoader(EXISTING_PET_LOADER, null, EditorActivity.this);
        }

        nameEditText = findViewById(R.id.editor_name_edit_text);
        breedEditText = findViewById(R.id.editor_breed_edit_text);
        genderSpinner = findViewById(R.id.editor_gender_spinner);
        weightEditText = findViewById(R.id.editor_weight_edit_text);

        nameEditText.setOnTouchListener(touchListener);
        breedEditText.setOnTouchListener(touchListener);
        genderSpinner.setOnTouchListener(touchListener);
        weightEditText.setOnTouchListener(touchListener);

        genderSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selection = (String) parent.getItemAtPosition(position);
                if(!TextUtils.isEmpty(selection)) {
                    if (selection.equals(getString(R.string.gender_male))) {
                        gender = PetEntry.GENDER_MALE;
                    } else if (selection.equals(getString(R.string.gender_female))) {
                        gender = PetEntry.GENDER_FEMALE;
                    } else {
                        gender = PetEntry.GENDER_UNKNOWN;
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                gender = PetEntry.GENDER_UNKNOWN;
            }
        });
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        if (currentPetUri == null) {
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_save:
                savePet();
                finish();
                return true;
            case R.id.action_delete:
                showDeleteConfirmationDialog();
                return true;
            case R.id.home:
                if (!petHasChanged) {
                    NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    return true;
                }

                DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    }
                };

                showUnsavedChangesDialog(listener);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void savePet() {
        String name = nameEditText.getText().toString().trim();
        String breed = breedEditText.getText().toString().trim();
        String weightString = weightEditText.getText().toString().trim();
        int weight = 0;

        if (!TextUtils.isEmpty(weightString)) {
            weight = Integer.parseInt(weightString);
        }

        if (currentPetUri == null
                && TextUtils.isEmpty(name)
                && TextUtils.isEmpty(breed)
                && weight == 0
                && gender == PetEntry.GENDER_UNKNOWN) {
            return;
        }

        ContentValues values = new ContentValues();
        values.put(PetEntry.COLUMN_NAME, name);
        values.put(PetEntry.COLUMN_BREED, breed);
        values.put(PetEntry.COLUMN_WEIGHT, weight);
        values.put(PetEntry.COLUMN_GENDER, gender);

        if (currentPetUri == null) {
            Uri newUri = getContentResolver().insert(PetEntry.CONTENT_URI, values);

            if (newUri == null) {
                Toast.makeText(this, getString(R.string.editor_insert_pet_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.editor_insert_pet_successful),
                        Toast.LENGTH_SHORT).show();
            }
        } else {
            int rowsAffected = getContentResolver().update(PetEntry.CONTENT_URI, values, null, null);

            if (rowsAffected == 0) {
                Toast.makeText(this, getString(R.string.editor_edit_pet_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.editor_edit_pet_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void showUnsavedChangesDialog(DialogInterface.OnClickListener discardButtonClickListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_message);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public void onBackPressed() {
        if (!petHasChanged) {
            super.onBackPressed();
            return;
        }

        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        };

        showUnsavedChangesDialog(listener);
    }

    private void showDeleteConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                deletePet();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void deletePet() {
        int rowsDeleted = 0;

        if (currentPetUri != null) {
            rowsDeleted = getContentResolver().delete(currentPetUri, null, null);
        }

        if (rowsDeleted == 0) {
            Toast.makeText(this, getString(R.string.editor_delete_failed),
                    Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, getString(R.string.editor_delete_successful),
                    Toast.LENGTH_SHORT).show();
        }

        finish();
    }


    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {
        String[] projection = {
                PetEntry.COLUMN_ID,
                PetEntry.COLUMN_NAME,
                PetEntry.COLUMN_BREED,
                PetEntry.COLUMN_GENDER,
                PetEntry.COLUMN_WEIGHT
        };

        return new CursorLoader(
                EditorActivity.this,
                currentPetUri,
                projection,
                null,
                null,
                null
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data == null || data.getCount() < 1) {
            return;
        }

        if (data.moveToFirst()) {
            String name = data.getString(data.getColumnIndex(PetEntry.COLUMN_NAME));
            String breed = data.getString(data.getColumnIndex(PetEntry.COLUMN_BREED));
            int weight = data.getInt(data.getColumnIndex(PetEntry.COLUMN_WEIGHT));
            int gender = data.getInt(data.getColumnIndex(PetEntry.COLUMN_GENDER));

            nameEditText.setText(name);
            breedEditText.setText(breed);
            weightEditText.setText(Integer.toString(weight));

            switch (gender) {
                case PetEntry.GENDER_MALE:
                    genderSpinner.setSelection(PetEntry.GENDER_MALE);
                    break;
                case PetEntry.GENDER_FEMALE:
                    genderSpinner.setSelection(PetEntry.GENDER_FEMALE);
                    break;
                default:
                    genderSpinner.setSelection(PetEntry.GENDER_UNKNOWN);
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        nameEditText.setText("");
        breedEditText.setText("");
        weightEditText.setText("");
        genderSpinner.setSelection(PetEntry.GENDER_UNKNOWN);
    }
}
