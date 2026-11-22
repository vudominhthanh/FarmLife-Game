using UnityEngine;

public class Chest : MonoBehaviour
{
    [SerializeField] private int capacity = 45;
    [SerializeField] private Animator animator;

    private Inventory inventory;
    private bool playerInRange;
    private bool isOpen;

    public Inventory Inventory => inventory;

    public void Awake()
    {
        inventory= new Inventory(capacity);
    }

    private void Update()
    {
        if (!playerInRange) return;
        if (Input.GetKeyDown(KeyCode.E)) Toggle();
        if (isOpen && Input.GetKeyDown(KeyCode.Escape)) Close();
    }

    private void OnTriggerEnter2D(Collider2D other)
    {
        if (other.GetComponent<Player>() != null)
        {
            playerInRange = true;
        }
    }

    private void Toggle() { if (isOpen) Close(); else Open(); }

    private void Open()
    {
        isOpen = true;
        if (animator != null) animator.SetBool("IsOpen", true);
        InventoryManager.Instance.OpenChest(inventory);
    }

    private void Close()
    {
        isOpen = false;
        if (animator != null) animator.SetBool("IsOpen", false);
        InventoryManager.Instance.CloseChest();
    }

    private void OnTriggerExit2D(Collider2D other) 
    {
        if (other.GetComponent<Player>() != null)
        {
            playerInRange = false;
            if (isOpen) Close();
        }
    }
}
