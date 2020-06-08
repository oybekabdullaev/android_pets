package com.example.pets;

import android.content.Context;
import android.database.Cursor;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.example.pets.database.PetContract;
import com.example.pets.database.PetContract.PetEntry;

public class PetCursorAdapter extends CursorAdapter {

    public PetCursorAdapter(Context context, Cursor c) {
        super(context, c, 0);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.item_pet, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        TextView nameTextView = view.findViewById(R.id.item_pet_name_text_ciew);
        TextView breedTextView = view.findViewById(R.id.item_pet_breed_text_view);

        String name = cursor.getString(cursor.getColumnIndex(PetEntry.COLUMN_NAME));
        String breed = cursor.getString(cursor.getColumnIndex(PetEntry.COLUMN_BREED));

        if (TextUtils.isEmpty(breed)) {
            breed = context.getString(R.string.unknown_breed);
        }

        nameTextView.setText(name);
        breedTextView.setText(breed);
    }
}
