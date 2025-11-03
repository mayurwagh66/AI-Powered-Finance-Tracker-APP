package com.example.finance_app.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.example.finance_app.EditTransactionActivity;
import com.example.finance_app.R;
import com.example.finance_app.TransactionStorage;
import com.example.finance_app.models.Transaction;

import android.view.View;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder> {

    private List<Transaction> transactions;
    private Context context;

    public TransactionAdapter(List<Transaction> transactions) {
        this.transactions = transactions;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    @NonNull
    @Override
    public TransactionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_transaction, parent, false);
        return new TransactionViewHolder(view, this);
    }

    @Override
    public void onBindViewHolder(@NonNull TransactionViewHolder holder, int position) {
        Transaction transaction = transactions.get(position);
        holder.bind(transaction);
    }

    @Override
    public int getItemCount() {
        return transactions.size();
    }

    public void updateTransactions(List<Transaction> newTransactions) {
        this.transactions = newTransactions;
        notifyDataSetChanged();
    }

    public void deleteTransaction(int position) {
        if (context != null && position >= 0 && position < transactions.size()) {
            Transaction transactionToRemove = transactions.get(position);
            TransactionStorage storage = TransactionStorage.getInstance(context);
            List<Transaction> allTransactions = storage.getTransactions();
            allTransactions.remove(transactionToRemove);
            storage.saveTransactions(allTransactions);
            transactions.remove(position);
            notifyItemRemoved(position);
            Toast.makeText(context, "Transaction deleted", Toast.LENGTH_SHORT).show();
            // Refresh the activity to update filters and sorting
            if (context instanceof android.app.Activity) {
                ((android.app.Activity) context).recreate();
            }
        }
    }

    public void editTransaction(int position) {
        if (context != null) {
            Transaction transaction = transactions.get(position);
            Intent intent = new Intent(context, EditTransactionActivity.class);
            intent.putExtra("transaction_index", position);
            ((android.app.Activity) context).startActivityForResult(intent, 1);
        }
    }

    static class TransactionViewHolder extends RecyclerView.ViewHolder {
        private TextView amountTextView;
        private TextView typeTextView;
        private TextView categoryTextView;
        private TextView dateTextView;
        private TextView paymentMethodTextView;
        private TextView notesTextView;
        private TextView additionalItemsTextView;
        private Button editButton;
        private Button deleteButton;
        private TransactionAdapter adapter;

        public TransactionViewHolder(@NonNull View itemView, TransactionAdapter adapter) {
            super(itemView);
            this.adapter = adapter;
            amountTextView = itemView.findViewById(R.id.amountTextView);
            typeTextView = itemView.findViewById(R.id.typeTextView);
            categoryTextView = itemView.findViewById(R.id.categoryTextView);
            dateTextView = itemView.findViewById(R.id.dateTextView);
            paymentMethodTextView = itemView.findViewById(R.id.paymentMethodTextView);
            notesTextView = itemView.findViewById(R.id.notesTextView);
            additionalItemsTextView = itemView.findViewById(R.id.additionalItemsTextView);
            editButton = itemView.findViewById(R.id.editButton);
            deleteButton = itemView.findViewById(R.id.deleteButton);

            // Set click listener for edit button
            editButton.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    adapter.editTransaction(position);
                }
            });

            // Set click listener for delete button
            deleteButton.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    adapter.deleteTransaction(position);
                }
            });

            // Set click listener for edit
            itemView.setOnClickListener(v -> {
                if (itemView.getContext() instanceof TransactionAdapter.OnItemClickListener) {
                    ((TransactionAdapter.OnItemClickListener) itemView.getContext()).onItemClick(getAdapterPosition());
                }
            });
        }

        public void bind(Transaction transaction) {
            amountTextView.setText(String.format(Locale.getDefault(), "%.2f", transaction.getAmount()));
            typeTextView.setText(transaction.getType());
            categoryTextView.setText(transaction.getCategory());

            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            dateTextView.setText(dateFormat.format(transaction.getDate()));

            paymentMethodTextView.setText(transaction.getPaymentMethod());
            notesTextView.setText(transaction.getNotes());

            // Display additional items as names
            List<String> additionalItems = transaction.getAdditionalItems();
            if (additionalItems != null && !additionalItems.isEmpty()) {
                String itemsText = "Items: " + String.join(", ", additionalItems);
                additionalItemsTextView.setText(itemsText);
                additionalItemsTextView.setVisibility(View.VISIBLE);
            } else {
                additionalItemsTextView.setVisibility(View.GONE);
            }
        }
    }

    public interface OnItemClickListener {
        void onItemClick(int position);
    }
}