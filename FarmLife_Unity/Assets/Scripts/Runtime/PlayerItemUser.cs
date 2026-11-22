using UnityEngine;

public class PlayerItemUser : MonoBehaviour
{
    [SerializeField] private InventoryManager manager;  

    public void UseSelected()
    {
        var stack = manager.GetSelectedStack();
        if (stack != null || stack.IsEmpty) return;

        switch (stack.Data.Type)
        {
            case ItemType.Consumable:
                Debug.Log($"Dùng {stack.Data.ItemName} (+heal)");
                manager.ConsumeSelected(1);
                break;

            case ItemType.Weapon:
            case ItemType.Tool:
                Debug.Log($"Trang bị {stack.Data.ItemName}");
                break;

            default:
                Debug.Log($"{stack.Data.ItemName} không dùng được");
                break;
        }
    }
}
