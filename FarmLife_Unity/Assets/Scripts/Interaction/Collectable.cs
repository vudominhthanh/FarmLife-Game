using UnityEngine;

[RequireComponent(typeof(Collider2D))]
public class Collectable : MonoBehaviour
{
    [SerializeField] private ItemData itemData;
    [SerializeField] private int amount = 1;

    private float pickupCooldown = 0.3f;

    public void Setup(ItemData data, int qty)
    {
        itemData = data;
        amount = qty;
        pickupCooldown = 0.3f;
    }

    private void Update()
    {
        if (pickupCooldown > 0)
        {
            pickupCooldown -= Time.deltaTime;
        }
    }

    private void OnTriggerEnter2D(Collider2D collision)
    {
        if (pickupCooldown > 0) return;
        if (!collision.CompareTag("Player")) return;
        if (itemData == null) return;

        int remaining = InventoryManager.Instance.Hotbar.AddItem(itemData, amount);
        if (remaining >= amount) return;

        amount = remaining;
        if (amount <= 0) Destroy(gameObject);
    }

}
