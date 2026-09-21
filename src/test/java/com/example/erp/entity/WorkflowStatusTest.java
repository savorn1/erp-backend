package com.example.erp.entity;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class WorkflowStatusTest {

    @Test
    void salesOrderLifecycleAllowsOnlyForwardBusinessTransitions() {
        assertThat(SalesOrderStatus.DRAFT.canTransitionTo(SalesOrderStatus.SUBMITTED)).isTrue();
        assertThat(SalesOrderStatus.CONFIRMED.canTransitionTo(SalesOrderStatus.PARTIALLY_DELIVERED)).isTrue();
        assertThat(SalesOrderStatus.PARTIALLY_DELIVERED.canTransitionTo(SalesOrderStatus.DELIVERED)).isTrue();
        assertThat(SalesOrderStatus.DELIVERED.canTransitionTo(SalesOrderStatus.CANCELLED)).isFalse();
        assertThat(SalesOrderStatus.CANCELLED.canTransitionTo(SalesOrderStatus.SUBMITTED)).isFalse();
    }

    @Test
    void purchaseOrderLifecycleStopsCancellationAfterReceivingBegins() {
        assertThat(PurchaseOrderStatus.APPROVED.canTransitionTo(PurchaseOrderStatus.SENT)).isTrue();
        assertThat(PurchaseOrderStatus.SENT.canTransitionTo(PurchaseOrderStatus.PARTIALLY_RECEIVED)).isTrue();
        assertThat(PurchaseOrderStatus.PARTIALLY_RECEIVED.canTransitionTo(PurchaseOrderStatus.RECEIVED)).isTrue();
        assertThat(PurchaseOrderStatus.PARTIALLY_RECEIVED.canTransitionTo(PurchaseOrderStatus.CANCELLED)).isFalse();
    }

    @Test
    void deliveryAndInvoiceTerminalStatesCannotBeChanged() {
        assertThat(DeliveryStatus.PACKED.canTransitionTo(DeliveryStatus.SHIPPED)).isTrue();
        assertThat(DeliveryStatus.SHIPPED.canTransitionTo(DeliveryStatus.DELIVERED)).isTrue();
        assertThat(DeliveryStatus.SHIPPED.canTransitionTo(DeliveryStatus.CANCELLED)).isFalse();
        assertThat(InvoiceStatus.APPROVED.canTransitionTo(InvoiceStatus.CANCELLED)).isTrue();
        assertThat(InvoiceStatus.CANCELLED.canTransitionTo(InvoiceStatus.APPROVED)).isFalse();
        assertThat(PurchaseInvoiceStatus.CANCELLED.canTransitionTo(PurchaseInvoiceStatus.APPROVED)).isFalse();
    }
}
